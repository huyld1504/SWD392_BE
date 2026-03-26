package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.CreateFeedingRequestDTO;
import com.swd392.dtos.requestDTO.UpdateFeedingRequestDTO;
import com.swd392.dtos.responseDTO.*;
import com.swd392.entities.*;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final SemesterRepository semesterRepository;

    // ==================== 1. CREATE FEEDING PERIOD ====================

    @Override
    @Transactional
    public FeedingPeriodResponseDTO createFeedingPeriod(CreateFeedingRequestDTO request, String adminEmail) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n    ├─ SERVICE ─ createFeedingPeriod\n    │ SemesterCode : {}\n    │ Admin        : {}",
            request.getSemesterCode(), adminEmail);

        User admin = userRepository.findByEmail(adminEmail)
            .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND));

        // Semester must already exist (created via CRUD API)
        Semester semester = semesterRepository.findBySemesterCode(request.getSemesterCode().toUpperCase())
            .orElseThrow(() -> new AppException(
                "Semester not found: " + request.getSemesterCode()
                    + ". Please create the semester first via POST /api/v1/semesters",
                HttpStatus.NOT_FOUND));

        // Cannot create feeding for a past semester (endDate < today)
        LocalDate today = LocalDate.now();
        if (today.isAfter(semester.getEndDate())) {
            throw new AppException(
                "Cannot create feeding for past semester " + semester.getSemesterCode()
                    + " (ended " + semester.getEndDate() + ")",
                HttpStatus.BAD_REQUEST);
        }

        // Check if feeding period already exists for this semester
        if (feedingPeriodRepository.existsBySemesterSemesterId(semester.getSemesterId())) {
            throw new AppException(
                "Feeding period already exists for semester " + semester.getSemesterCode(),
                HttpStatus.CONFLICT);
        }

        BigDecimal grantAmount = request.getGrantAmount() != null
            ? request.getGrantAmount()
            : DEFAULT_GRANT_AMOUNT;

        FeedingPeriod period = new FeedingPeriod();
        period.setSemester(semester);
        period.setGrantAmount(grantAmount);
        period.setStatus(FeedingPeriod.PeriodStatus.ACTIVE);
        period.setCreatedBy(admin);
        feedingPeriodRepository.save(period);

        log.info("\n    └─ SERVICE ─ createFeedingPeriod\n      Status   : ACTIVE\n      Period   : id={}\n      Semester : {} ({} → {})",
            period.getPeriodId(), semester.getSemesterCode(), semester.getStartDate(), semester.getEndDate());

        return mapToResponseDTO(period);
    }

    // ==================== 2. UPDATE FEEDING PERIOD ====================

    @Override
    @Transactional
    public FeedingPeriodResponseDTO updateFeedingPeriod(Integer periodId, UpdateFeedingRequestDTO request) {
        log.info("\n    ├─ SERVICE ─ updateFeedingPeriod\n    │ PeriodId : {}", periodId);

        FeedingPeriod period = feedingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

        if (period.getStatus() != FeedingPeriod.PeriodStatus.ACTIVE) {
            throw new AppException("Can only update ACTIVE periods. Current: " + period.getStatus(),
                HttpStatus.BAD_REQUEST);
        }

        if (request.getGrantAmount() != null) {
            period.setGrantAmount(request.getGrantAmount());
        }

        feedingPeriodRepository.save(period);
        log.info("\n    └─ SERVICE ─ updateFeedingPeriod\n      Updated grantAmount to: {}", period.getGrantAmount());

        return mapToResponseDTO(period);
    }

    // ==================== 3. COMPLETE FEEDING PERIOD ====================

    @Override
    @Transactional
    public FeedingPeriodResponseDTO completeFeedingPeriod(Integer periodId) {
        log.info("\n    ├─ SERVICE ─ completeFeedingPeriod\n    │ PeriodId : {}", periodId);

        FeedingPeriod period = feedingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

        if (period.getStatus() != FeedingPeriod.PeriodStatus.ACTIVE) {
            throw new AppException("Can only complete ACTIVE periods. Current: " + period.getStatus(),
                HttpStatus.BAD_REQUEST);
        }

        period.setStatus(FeedingPeriod.PeriodStatus.COMPLETED);
        // Also complete the semester
        Semester semester = period.getSemester();
        semester.setStatus(Semester.SemesterStatus.COMPLETED);
        semesterRepository.save(semester);
        feedingPeriodRepository.save(period);

        log.info("\n    └─ SERVICE ─ completeFeedingPeriod\n      Period {} → COMPLETED", periodId);

        return mapToResponseDTO(period);
    }

    // ==================== 4. CANCEL FEEDING PERIOD ====================

    @Override
    @Transactional
    public void cancelFeedingPeriod(Integer periodId) {
        log.info("\n    ├─ SERVICE ─ cancelFeedingPeriod\n    │ PeriodId : {}", periodId);

        FeedingPeriod period = feedingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

        if (period.getStatus() != FeedingPeriod.PeriodStatus.ACTIVE) {
            throw new AppException("Can only cancel ACTIVE periods", HttpStatus.BAD_REQUEST);
        }

        if (period.getTotalUsersFed() > 0) {
            throw new AppException(
                "Cannot cancel: " + period.getTotalUsersFed() + " users have already been fed",
                HttpStatus.BAD_REQUEST);
        }

        period.setStatus(FeedingPeriod.PeriodStatus.CANCELLED);
        feedingPeriodRepository.save(period);

        log.info("\n    └─ SERVICE ─ cancelFeedingPeriod\n      Period {} → CANCELLED", periodId);
    }

    // ==================== 5. TRIGGER FEEDING (Manual) ====================

    @Override
    @Transactional
    public FeedingPeriodResponseDTO triggerFeeding(Integer periodId) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n    ├─ SERVICE ─ triggerFeeding (Manual)\n    │ PeriodId : {}", periodId);

        FeedingPeriod period = feedingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

        if (period.getStatus() != FeedingPeriod.PeriodStatus.ACTIVE) {
            throw new AppException("Can only trigger ACTIVE periods. Current: " + period.getStatus(),
                HttpStatus.BAD_REQUEST);
        }

        // Check if semester has started
        Semester semester = period.getSemester();
        LocalDate today = LocalDate.now();

        if (today.isBefore(semester.getStartDate())) {
            throw new AppException(
                "Cannot trigger feeding: semester " + semester.getSemesterCode()
                    + " has not started yet (starts " + semester.getStartDate() + ")",
                HttpStatus.BAD_REQUEST);
        }

        if (today.isAfter(semester.getEndDate())) {
            throw new AppException(
                "Cannot trigger feeding: semester " + semester.getSemesterCode()
                    + " has already ended (" + semester.getEndDate() + ")",
                HttpStatus.BAD_REQUEST);
        }

        int processed = processFeedingForPeriod(period);

        FeedingPeriodResponseDTO response = mapToDetailResponseDTO(period);
        response.setUsersProcessedNow(processed);
        return response;
    }

    // ==================== 6. DAILY FEEDING (Scheduler) ====================

    @Override
    @Transactional
    public int executeDailyFeeding() {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n    ├─ SERVICE ─ executeDailyFeeding");

        LocalDate today = LocalDate.now();

        // Find all ACTIVE feeding periods where today is within the semester date range
        List<FeedingPeriod> activePeriods = feedingPeriodRepository.findByStatus(FeedingPeriod.PeriodStatus.ACTIVE);

        int totalProcessed = 0;

        for (FeedingPeriod period : activePeriods) {
            Semester semester = period.getSemester();

            // Check if today is within this semester's date range
            if (!today.isBefore(semester.getStartDate()) && !today.isAfter(semester.getEndDate())) {
                log.info("\n    │ Processing period: {} ({})", period.getPeriodId(), semester.getSemesterCode());
                int processed = processFeedingForPeriod(period);
                totalProcessed += processed;
            }
        }

        log.info("\n    └─ SERVICE ─ executeDailyFeeding\n      Total users fed today: {}", totalProcessed);
        return totalProcessed;
    }

    // ==================== 7. GET ALL FEEDING PERIODS ====================

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDTO<List<FeedingPeriodResponseDTO>> getAllFeedingPeriods(
        int page, int size, String status, String semesterCode) {

        log.info("\n    ├─ SERVICE ─ getAllFeedingPeriods\n    │ Page : {}, Size : {}, Status : {}, Semester : {}",
            page, size, status, semesterCode);

        Specification<FeedingPeriod> spec = Specification.where(null);

        if (status != null && !status.isBlank()) {
            FeedingPeriod.PeriodStatus periodStatus =
                FeedingPeriod.PeriodStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("status"), periodStatus));
        }

        if (semesterCode != null && !semesterCode.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("semester").get("semesterCode"), semesterCode.toUpperCase()));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FeedingPeriod> periodPage = feedingPeriodRepository.findAll(spec, pageable);

        List<FeedingPeriodResponseDTO> dtos = periodPage.getContent().stream()
            .map(this::mapToResponseDTO)
            .toList();

        return PaginationResponseDTO.<List<FeedingPeriodResponseDTO>>builder()
            .totalItems(periodPage.getTotalElements())
            .totalPages(periodPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .data(dtos)
            .build();
    }

    // ==================== 8. GET FEEDING DETAIL ====================

    @Override
    @Transactional(readOnly = true)
    public FeedingPeriodResponseDTO getFeedingDetail(Integer periodId) {
        log.info("\n    ├─ SERVICE ─ getFeedingDetail\n    │ Period : {}", periodId);

        FeedingPeriod period = feedingPeriodRepository.findById(periodId)
            .orElseThrow(() -> new AppException("Feeding period not found", HttpStatus.NOT_FOUND));

        return mapToDetailResponseDTO(period);
    }

    // ==================== CORE FEEDING LOGIC ====================

    /**
     * Process feeding for a single period: find unfed users and give them coins.
     * Returns the number of users processed in this run.
     */
    private int processFeedingForPeriod(FeedingPeriod period) {
        // Get system wallet
        Wallet systemWallet = walletRepository.findByWalletType(Wallet.WalletType.SYSTEM)
            .orElseThrow(() -> new AppException(
                "System wallet not found. Please initialize it first.", HttpStatus.NOT_FOUND));

        // Get IDs of users already fed in this period
        Set<Long> fedUserIds = userFeedingRepository
            .findFedUserIdsByPeriodId(period.getPeriodId())
            .stream().collect(Collectors.toSet());

        // Get ACTIVE users who haven't been fed yet
        List<User> unfedUsers = userRepository.findAll().stream()
            .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
            .filter(u -> !fedUserIds.contains(u.getUserId()))
            .toList();

        if (unfedUsers.isEmpty()) {
            log.info("\n    │ No unfed users found for period {}", period.getPeriodId());
            return 0;
        }

        BigDecimal coinsNeeded = period.getGrantAmount()
            .multiply(BigDecimal.valueOf(unfedUsers.size()));

        // Check system wallet balance
        if (systemWallet.getBalance().compareTo(coinsNeeded) < 0) {
            BigDecimal deficit = coinsNeeded.subtract(systemWallet.getBalance());
            log.warn("\n    │ ⚠️ INSUFFICIENT BALANCE for period {}"
                    + "\n    │   Users to feed : {}"
                    + "\n    │   Coins needed  : {}"
                    + "\n    │   System balance: {}"
                    + "\n    │   DEFICIT       : {}",
                period.getPeriodId(), unfedUsers.size(), coinsNeeded,
                systemWallet.getBalance(), deficit);
            throw new AppException(
                "System wallet insufficient. Need: " + coinsNeeded
                    + ", Have: " + systemWallet.getBalance()
                    + ", Deficit: " + deficit,
                HttpStatus.BAD_REQUEST);
        }

        log.info("\n    │ Feeding {} unfed users × {} coins = {} total",
            unfedUsers.size(), period.getGrantAmount(), coinsNeeded);

        int processed = 0;
        Semester semester = period.getSemester();

        for (User user : unfedUsers) {
            // Find or create MAIN wallet
            Wallet mainWallet = walletRepository
                .findByUserAndWalletType(user, Wallet.WalletType.MAIN)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setWalletType(Wallet.WalletType.MAIN);
                    newWallet.setCurrency(Wallet.Currency.BLUE);
                    newWallet.setStatus(Wallet.WalletStatus.ACTIVE);
                    newWallet.setBalance(BigDecimal.ZERO);
                    walletRepository.save(newWallet);
                    log.info("\n    │ CREATED MAIN wallet for: {}", user.getEmail());
                    return newWallet;
                });

            // Add coins to user wallet
            mainWallet.setBalance(mainWallet.getBalance().add(period.getGrantAmount()));
            walletRepository.save(mainWallet);

            // Deduct from system wallet
            systemWallet.setBalance(systemWallet.getBalance().subtract(period.getGrantAmount()));

            // Create FEEDING transaction (linked to semester)
            Transaction transaction = new Transaction();
            transaction.setSenderWallet(systemWallet);
            transaction.setReceiverWallet(mainWallet);
            transaction.setAmount(period.getGrantAmount());
            transaction.setCurrency(Transaction.Currency.BLUE);
            transaction.setTransactionType(Transaction.TransactionType.FEEDING);
            transaction.setSemester(semester);
            transactionRepository.save(transaction);

            // Create UserFeeding record
            UserFeeding feeding = new UserFeeding();
            feeding.setFeedingPeriod(period);
            feeding.setUser(user);
            feeding.setTransaction(transaction);
            feeding.setAmountReceived(period.getGrantAmount());
            feeding.setFedAt(LocalDateTime.now());
            userFeedingRepository.save(feeding);

            processed++;
        }

        // Save updated system wallet
        walletRepository.save(systemWallet);

        // Update period running stats
        period.setTotalCoinsFed(period.getTotalCoinsFed().add(coinsNeeded));
        period.setTotalUsersFed(period.getTotalUsersFed() + processed);
        feedingPeriodRepository.save(period);

        log.info("\n    │ ✅ Fed {} users | System balance: {} | Period total: {} users, {} coins",
            processed, systemWallet.getBalance(), period.getTotalUsersFed(), period.getTotalCoinsFed());

        return processed;
    }

    // ==================== HELPERS ====================

    /**
     * Map for list view (no users detail, no dashboard stats).
     */
    private FeedingPeriodResponseDTO mapToResponseDTO(FeedingPeriod period) {
        Semester s = period.getSemester();
        return FeedingPeriodResponseDTO.builder()
            .periodId(period.getPeriodId())
            .semesterCode(s.getSemesterCode())
            .semesterName(s.getSemesterName())
            .startDate(s.getStartDate())
            .endDate(s.getEndDate())
            .grantAmount(period.getGrantAmount())
            .status(period.getStatus().name())
            .createdBy(period.getCreatedBy() != null ? mapToUserInfo(period.getCreatedBy()) : null)
            .createdAt(period.getCreatedAt())
            .updatedAt(period.getUpdatedAt())
            .totalUsersFed(period.getTotalUsersFed())
            .totalCoinsFed(period.getTotalCoinsFed())
            .build();
    }

    /**
     * Map for detail view (with dashboard stats and users list).
     */
    private FeedingPeriodResponseDTO mapToDetailResponseDTO(FeedingPeriod period) {
        FeedingPeriodResponseDTO dto = mapToResponseDTO(period);

        // Build user details
        List<UserFeeding> userFeedings = userFeedingRepository
            .findByFeedingPeriodPeriodId(period.getPeriodId());

        List<UserFeedingDetailDTO> userDetails = userFeedings.stream()
            .map(uf -> new UserFeedingDetailDTO(
                uf.getFeedingId(),
                mapToUserInfo(uf.getUser()),
                uf.getAmountReceived(),
                uf.getFedAt()))
            .sorted((a, b) -> b.fedAt().compareTo(a.fedAt()))  // newest first
            .toList();

        dto.setUsers(userDetails);

        // Build dashboard stats
        long totalActiveUsers = userRepository.findAll().stream()
            .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
            .count();

        int pendingUsers = (int) totalActiveUsers - period.getTotalUsersFed();
        if (pendingUsers < 0) pendingUsers = 0;

        BigDecimal estimatedNeeded = period.getGrantAmount()
            .multiply(BigDecimal.valueOf(pendingUsers));

        BigDecimal systemBalance = BigDecimal.ZERO;
        Optional<Wallet> systemWalletOpt = walletRepository.findByWalletType(Wallet.WalletType.SYSTEM);
        if (systemWalletOpt.isPresent()) {
            systemBalance = systemWalletOpt.get().getBalance();
        }

        BigDecimal deficit = estimatedNeeded.subtract(systemBalance);
        if (deficit.compareTo(BigDecimal.ZERO) < 0) {
            deficit = BigDecimal.ZERO; // no deficit
        }

        dto.setStats(new FeedingStatsDTO(
            period.getTotalUsersFed(),
            period.getTotalCoinsFed(),
            pendingUsers,
            estimatedNeeded,
            systemBalance,
            deficit
        ));

        return dto;
    }

    private UserInfoDTO mapToUserInfo(User user) {
        return new UserInfoDTO(user.getUserId(), user.getFullName(), user.getEmail(), user.getAvatarUrl());
    }
}
