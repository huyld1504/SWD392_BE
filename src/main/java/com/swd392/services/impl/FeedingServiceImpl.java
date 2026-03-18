package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.responseDTO.*;
import com.swd392.entities.*;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.*;
import com.swd392.repositories.specifications.FeedingPeriodSpecification;
import com.swd392.services.interfaces.FeedingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedingServiceImpl implements FeedingService {

  private static final BigDecimal DEFAULT_GRANT_AMOUNT = new BigDecimal("100");

  private final FeedingPeriodRepository feedingPeriodRepository;
  private final UserFeedingRepository userFeedingRepository;
  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;

  // ==================== 1. ADMIN SCHEDULE ====================

  @Override
  @Transactional
  public FeedingPeriodResponseDTO scheduleFeedingReset(LocalDateTime scheduledAt, String adminEmail) {
    log.info("\n    ├─ SERVICE ─ scheduleFeedingReset\n    │ ScheduledAt : {}\n    │ Admin       : {}",
        scheduledAt, adminEmail);

    User admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND));

    if (feedingPeriodRepository.existsByStatus(FeedingPeriod.PeriodStatus.PENDING)) {
      throw new AppException(
          "There is already a PENDING feeding period. Cancel it first before creating a new one.",
          HttpStatus.CONFLICT);
    }

    YearMonth targetMonth = YearMonth.from(scheduledAt);
    FeedingPeriod period = new FeedingPeriod();
    period.setPeriodName("Feeding " + targetMonth);
    period.setGrantAmount(DEFAULT_GRANT_AMOUNT);
    period.setScheduledAt(scheduledAt);
    period.setStatus(FeedingPeriod.PeriodStatus.PENDING);
    period.setTriggerSource("MANUAL_ADMIN");
    period.setCreatedBy(admin);
    feedingPeriodRepository.save(period);

    log.info("\n    └─ SERVICE ─ scheduleFeedingReset\n      Status : PENDING\n      Period : id={}",
        period.getPeriodId());

    return mapToResponseDTO(period, null, null);
  }

  // ==================== 1b. UPDATE SCHEDULE ====================

  @Override
  @Transactional
  public FeedingPeriodResponseDTO updateFeedingSchedule(Integer periodId, LocalDateTime newScheduledAt) {
    log.info("\n    ├─ SERVICE ─ updateFeedingSchedule\n    │ PeriodId    : {}\n    │ NewSchedule : {}",
        periodId, newScheduledAt);

    FeedingPeriod period = feedingPeriodRepository.findById(periodId)
        .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

    if (period.getStatus() != FeedingPeriod.PeriodStatus.PENDING) {
      throw new AppException(
          "Can only update schedule for PENDING periods. Current status: " + period.getStatus(),
          HttpStatus.BAD_REQUEST);
    }

    YearMonth targetMonth = YearMonth.from(newScheduledAt);
    period.setScheduledAt(newScheduledAt);
    period.setPeriodName("Feeding " + targetMonth);
    feedingPeriodRepository.save(period);

    log.info("\n    └─ SERVICE ─ updateFeedingSchedule\n      Updated scheduledAt to: {}", newScheduledAt);

    return mapToResponseDTO(period, null, null);
  }

  // ==================== 1c. DELETE FEEDING PERIOD ====================

  @Override
  @Transactional
  public void deleteFeedingPeriod(Integer periodId) {
    log.info("\n    ├─ SERVICE ─ deleteFeedingPeriod\n    │ PeriodId : {}", periodId);

    FeedingPeriod period = feedingPeriodRepository.findById(periodId)
        .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

    if (period.getStatus() != FeedingPeriod.PeriodStatus.PENDING) {
      throw new AppException(
          "Can only delete PENDING periods. Current status: " + period.getStatus(),
          HttpStatus.BAD_REQUEST);
    }

    feedingPeriodRepository.delete(period);

    log.info("\n    └─ SERVICE ─ deleteFeedingPeriod\n      Deleted period: id={}", periodId);
  }

  // ==================== 2. EXECUTE IMMEDIATELY ====================

  @Override
  @Transactional
  public FeedingPeriodResponseDTO executeFeedingReset(String triggerSource, String adminEmail) {
    RequestContext.setCurrentLayer("SERVICE");
    log.info("\n    ├─ SERVICE ─ executeFeedingReset\n    │ Trigger : {}\n    │ Admin   : {}",
        triggerSource, adminEmail);

    if (feedingPeriodRepository.existsByStatus(FeedingPeriod.PeriodStatus.EXECUTING)) {
      throw new AppException("A feeding reset is already in progress.", HttpStatus.CONFLICT);
    }

    YearMonth currentMonth = YearMonth.now();
    LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
    LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

    List<FeedingPeriod> completedThisMonth = feedingPeriodRepository
        .findByStatusAndExecutedAtBetween(FeedingPeriod.PeriodStatus.COMPLETED, monthStart, monthEnd);

    if (!completedThisMonth.isEmpty()) {
      throw new AppException("Feeding reset already completed this month (" + currentMonth + ")", HttpStatus.CONFLICT);
    }

    User admin = null;
    if (adminEmail != null) {
      admin = userRepository.findByEmail(adminEmail).orElse(null);
    }

    FeedingPeriod period = new FeedingPeriod();
    period.setPeriodName("Feeding " + currentMonth);
    period.setGrantAmount(DEFAULT_GRANT_AMOUNT);
    period.setScheduledAt(LocalDateTime.now());
    period.setStatus(FeedingPeriod.PeriodStatus.EXECUTING);
    period.setTriggerSource(triggerSource);
    period.setCreatedBy(admin);
    feedingPeriodRepository.save(period);

    return processFeeding(period);
  }

  // ==================== 3. EXECUTE PENDING PERIOD ====================

  @Override
  @Transactional
  public FeedingPeriodResponseDTO executePendingPeriod(Integer periodId) {
    log.info("\n    ├─ SERVICE ─ executePendingPeriod\n    │ Period : {}", periodId);

    FeedingPeriod period = feedingPeriodRepository.findById(periodId)
        .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

    if (period.getStatus() != FeedingPeriod.PeriodStatus.PENDING) {
      throw new AppException("Period is not in PENDING status: " + period.getStatus(), HttpStatus.BAD_REQUEST);
    }

    if (feedingPeriodRepository.existsByStatus(FeedingPeriod.PeriodStatus.EXECUTING)) {
      throw new AppException("Another feeding is already executing", HttpStatus.CONFLICT);
    }

    period.setStatus(FeedingPeriod.PeriodStatus.EXECUTING);
    feedingPeriodRepository.save(period);

    return processFeeding(period);
  }

  // ==================== 4. GET ALL FEEDING PERIODS ====================

  @Override
  @Transactional(readOnly = true)
  public PaginationResponseDTO<List<FeedingPeriodResponseDTO>> getAllFeedingPeriods(
      int page, int size, LocalDateTime fromDate, LocalDateTime toDate, String status, String triggerSource) {
    log.info(
        "\n    ├─ SERVICE ─ getAllFeedingPeriods\n    │ Page    : {}\n    │ Size    : {}\n    │ From    : {}\n    │ To      : {}\n    │ Status  : {}\n    │ Trigger : {}",
        page, size, fromDate, toDate, status, triggerSource);

    Specification<FeedingPeriod> spec = Specification.where(null);

    if (status != null && !status.isBlank()) {
      spec = spec.and(FeedingPeriodSpecification.hasStatus(
          FeedingPeriod.PeriodStatus.valueOf(status.toUpperCase())));
    }
    if (triggerSource != null && !triggerSource.isBlank()) {
      spec = spec.and(FeedingPeriodSpecification.hasTriggerSource(triggerSource));
    }
    if (fromDate != null) {
      spec = spec.and(FeedingPeriodSpecification.createdAfter(fromDate));
    }
    if (toDate != null) {
      spec = spec.and(FeedingPeriodSpecification.createdBefore(toDate));
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<FeedingPeriod> periodPage = feedingPeriodRepository.findAll(spec, pageable);

    List<FeedingPeriodResponseDTO> dtos = periodPage.getContent().stream()
        .map(period -> {
          int totalStudents = userFeedingRepository.findByFeedingPeriodPeriodId(period.getPeriodId()).size();
          return mapToResponseDTOForList(period, totalStudents);
        })
        .toList();

    log.info("\n    └─ SERVICE ─ getAllFeedingPeriods\n      Total : {}", periodPage.getTotalElements());

    return PaginationResponseDTO.<List<FeedingPeriodResponseDTO>>builder()
        .totalItems(periodPage.getTotalElements())
        .totalPages(periodPage.getTotalPages())
        .currentPage(page)
        .pageSize(size)
        .data(dtos)
        .build();
  }

  // ==================== 5. GET FEEDING DETAIL ====================

  @Override
  @Transactional(readOnly = true)
  public FeedingPeriodResponseDTO getFeedingDetail(Integer periodId) {
    log.info("\n    ├─ SERVICE ─ getFeedingDetail\n    │ Period : {}", periodId);

    FeedingPeriod period = feedingPeriodRepository.findById(periodId)
        .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

    List<UserFeeding> userFeedings = userFeedingRepository.findByFeedingPeriodPeriodId(periodId);

    // Sort by snapshotEarnedBalance DESC → top donated students first
    List<UserFeedingDetailDTO> userDetails = userFeedings.stream()
        .map(uf -> new UserFeedingDetailDTO(
            uf.getFeedingId(),
            mapToUserInfo(uf.getUser()),
            uf.getAmountReceived(),
            uf.getSnapshotEarnedBalance()))
        .sorted((a, b) -> b.snapshotEarnedBalance().compareTo(a.snapshotEarnedBalance()))
        .toList();

    // Calculate summary totals
    BigDecimal totalCoinsGranted = userFeedings.stream()
        .map(UserFeeding::getAmountReceived)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCoinsEarned = userFeedings.stream()
        .map(UserFeeding::getSnapshotEarnedBalance)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info(
        "\n    └─ SERVICE ─ getFeedingDetail\n      Users            : {}\n      Total Granted     : {}\n      Total Earned      : {}",
        userDetails.size(), totalCoinsGranted, totalCoinsEarned);

    return FeedingPeriodResponseDTO.builder()
        .periodId(period.getPeriodId())
        .periodName(period.getPeriodName())
        .grantAmount(period.getGrantAmount())
        .status(period.getStatus().name())
        .triggerSource(period.getTriggerSource())
        .createdBy(period.getCreatedBy() != null ? mapToUserInfo(period.getCreatedBy()) : null)
        .scheduledAt(period.getScheduledAt())
        .executedAt(period.getExecutedAt())
        .createdAt(period.getCreatedAt())
        .totalUsers(userDetails.size())
        .totalCoinsGranted(totalCoinsGranted)
        .totalCoinsEarned(totalCoinsEarned)
        .users(userDetails)
        .build();
  }

  // ==================== CORE LOGIC ====================

  private FeedingPeriodResponseDTO processFeeding(FeedingPeriod period) {
    int processed = 0;
    int skipped = 0;

    try {
      // Get system wallet as the source of coins
      Wallet systemWallet = walletRepository.findByWalletType(Wallet.WalletType.SYSTEM)
          .orElseThrow(() -> new AppException("System wallet not found. Please initialize it first.", HttpStatus.NOT_FOUND));

      List<User> activeUsers = userRepository.findAll().stream()
          .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
          .toList();

      // Validate system wallet has enough balance
      BigDecimal totalRequired = period.getGrantAmount().multiply(BigDecimal.valueOf(activeUsers.size()));
      if (systemWallet.getBalance().compareTo(totalRequired) < 0) {
        throw new AppException(
            "System wallet has insufficient balance. Required: " + totalRequired
                + ", Available: " + systemWallet.getBalance(),
            HttpStatus.BAD_REQUEST);
      }

      log.info("\n    │ Found {} active users | System wallet balance: {} | Required: {}",
          activeUsers.size(), systemWallet.getBalance(), totalRequired);

      for (User user : activeUsers) {
        Optional<Wallet> walletOpt = walletRepository
            .findByUserAndWalletType(user, Wallet.WalletType.MAIN);

        Wallet mainWallet;
        if (walletOpt.isEmpty()) {
          // Auto-create MAIN wallet for user who doesn't have one
          mainWallet = new Wallet();
          mainWallet.setUser(user);
          mainWallet.setWalletType(Wallet.WalletType.MAIN);
          mainWallet.setCurrency(Wallet.Currency.BLUE);
          mainWallet.setStatus(Wallet.WalletStatus.ACTIVE);
          mainWallet.setBalance(BigDecimal.ZERO);
          walletRepository.save(mainWallet);
          log.info("\n    │ CREATED MAIN wallet for: {}", user.getEmail());
        } else {
          mainWallet = walletOpt.get();
        }

        BigDecimal snapshotEarned = BigDecimal.ZERO;
        Optional<Wallet> earnedOpt = walletRepository
            .findByUserAndWalletType(user, Wallet.WalletType.EARNED);
        if (earnedOpt.isPresent()) {
          snapshotEarned = earnedOpt.get().getBalance();
        }

        // Reset user wallet to grant amount
        mainWallet.setBalance(period.getGrantAmount());
        walletRepository.save(mainWallet);

        // Deduct from system wallet
        systemWallet.setBalance(systemWallet.getBalance().subtract(period.getGrantAmount()));

        // Create transaction: System Wallet → User Wallet
        Transaction transaction = new Transaction();
        transaction.setSenderWallet(systemWallet);
        transaction.setReceiverWallet(mainWallet);
        transaction.setAmount(period.getGrantAmount());
        transaction.setCurrency(Transaction.Currency.BLUE);
        transaction.setTransactionType(Transaction.TransactionType.FEEDING);
        transactionRepository.save(transaction);

        UserFeeding feeding = new UserFeeding();
        feeding.setFeedingPeriod(period);
        feeding.setUser(user);
        feeding.setTransaction(transaction);
        feeding.setSnapshotEarnedBalance(snapshotEarned);
        feeding.setAmountReceived(period.getGrantAmount());
        userFeedingRepository.save(feeding);

        processed++;
      }

      // Save updated system wallet balance
      walletRepository.save(systemWallet);

      period.setStatus(FeedingPeriod.PeriodStatus.COMPLETED);
      period.setExecutedAt(LocalDateTime.now());
      feedingPeriodRepository.save(period);

      log.info(
          "\n    └─ SERVICE ─ processFeeding\n      Status           : COMPLETED\n      Processed        : {} users\n      System balance   : {}",
          processed, systemWallet.getBalance());

    } catch (Exception e) {
      period.setStatus(FeedingPeriod.PeriodStatus.FAILED);
      period.setExecutedAt(LocalDateTime.now());
      feedingPeriodRepository.save(period);

      log.error("\n    └─ SERVICE ─ processFeeding\n      Status : FAILED\n      Error  : {}", e.getMessage());
      throw new AppException("Feeding reset failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return mapToResponseDTO(period, processed, skipped);
  }

  // ==================== HELPERS ====================

  /**
   * Map for trigger/schedule result (with processed/skipped counts).
   */
  private FeedingPeriodResponseDTO mapToResponseDTO(FeedingPeriod period, Integer processed, Integer skipped) {
    return FeedingPeriodResponseDTO.builder()
        .periodId(period.getPeriodId())
        .periodName(period.getPeriodName())
        .grantAmount(period.getGrantAmount())
        .status(period.getStatus().name())
        .triggerSource(period.getTriggerSource())
        .createdBy(period.getCreatedBy() != null ? mapToUserInfo(period.getCreatedBy()) : null)
        .scheduledAt(period.getScheduledAt())
        .executedAt(period.getExecutedAt())
        .createdAt(period.getCreatedAt())
        .totalUsersProcessed(processed)
        .totalUsersSkipped(skipped)
        .build();
  }

  /**
   * Map for list view (no users detail, no processed/skipped).
   */
  private FeedingPeriodResponseDTO mapToResponseDTOForList(FeedingPeriod period, int totalUsers) {
    return FeedingPeriodResponseDTO.builder()
        .periodId(period.getPeriodId())
        .periodName(period.getPeriodName())
        .grantAmount(period.getGrantAmount())
        .status(period.getStatus().name())
        .triggerSource(period.getTriggerSource())
        .createdBy(period.getCreatedBy() != null ? mapToUserInfo(period.getCreatedBy()) : null)
        .scheduledAt(period.getScheduledAt())
        .executedAt(period.getExecutedAt())
        .createdAt(period.getCreatedAt())
        .totalUsers(totalUsers)
        .build();
  }

  private UserInfoDTO mapToUserInfo(User user) {
    return new UserInfoDTO(user.getUserId(), user.getFullName(), user.getEmail(), user.getAvatarUrl());
  }
}
