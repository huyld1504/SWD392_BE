package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.UpdateWalletStatusRequestDTO;
import com.swd392.dtos.responseDTO.TransactionResponseDTO;
import com.swd392.dtos.responseDTO.WalletResponseDTO;
import com.swd392.entities.Transaction;
import com.swd392.entities.User;
import com.swd392.entities.Wallet;
import com.swd392.exceptions.AppException;
import com.swd392.mapper.WalletMapper;
import com.swd392.repositories.TransactionRepository;
import com.swd392.repositories.UserRepository;
import com.swd392.repositories.WalletRepository;
import com.swd392.repositories.specifications.WalletSpecification;
import com.swd392.repositories.specifications.TransactionSpecification;
import com.swd392.services.interfaces.WalletService;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final WalletMapper walletMapper;

  @Override
  public void createDefaultWallet(User user) {
    RequestContext.setCurrentLayer("SERVICE");
    log.info("\n    ├─ SERVICE ─ createDefaultWallet\n    │ User : {}", user.getEmail());

    // Skip if user already has a MAIN wallet (e.g., login after registration)
    if (walletRepository.existsByUserAndWalletType(user, Wallet.WalletType.MAIN)) {
      log.info("\n    └─ SERVICE ─ createDefaultWallet\n      Status : SKIPPED (wallet already exists)");
      return;
    }

    Wallet wallet = new Wallet();
    wallet.setUser(user);
    wallet.setWalletType(Wallet.WalletType.MAIN);
    wallet.setCurrency(Wallet.Currency.BLUE);
    wallet.setBalance(new BigDecimal("100"));
    wallet.setStatus(Wallet.WalletStatus.ACTIVE);

    walletRepository.save(wallet);
    log.info(
        "\n    └─ SERVICE ─ createDefaultWallet\n      Status   : SUCCESS\n      Type     : MAIN\n      Currency : BLUE");
  }

  @Override
  public WalletResponseDTO createEarnedWallet(String email) {
    RequestContext.setCurrentLayer("SERVICE");
    log.info("\n    ├─ SERVICE ─ createEarnedWallet\n    │ User : {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

    // Check if user already has an EARNED wallet
    if (walletRepository.existsByUserAndWalletType(user, Wallet.WalletType.EARNED)) {
      throw new AppException("User already has an EARNED wallet", HttpStatus.CONFLICT);
    }

    Wallet wallet = new Wallet();
    wallet.setUser(user);
    wallet.setWalletType(Wallet.WalletType.EARNED);
    wallet.setCurrency(Wallet.Currency.GOLD);
    wallet.setBalance(new BigDecimal(100));
    wallet.setStatus(Wallet.WalletStatus.ACTIVE);

    Wallet saved = walletRepository.save(wallet);
    log.info(
        "\n    └─ SERVICE ─ createEarnedWallet\n      Status   : SUCCESS\n      Wallet   : id={}\n      Type     : EARNED\n      Currency : GOLD\n      Balance  : {}",
        saved.getWalletId(), saved.getBalance());

    return walletMapper.toDTO(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<WalletResponseDTO> getWalletsByCurrentUser(String email) {
    RequestContext.setCurrentLayer("SERVICE");
    log.info("\n    ├─ SERVICE ─ getWalletsByCurrentUser\n    │ User : {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

    List<Wallet> wallets = walletRepository.findAllByUserUserId(user.getUserId());

    log.info("\n    └─ SERVICE ─ getWalletsByCurrentUser\n      Status : SUCCESS\n      Count  : {} wallet(s)",
        wallets.size());

    return wallets.stream()
        .map(walletMapper::toDTO)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponseDTO<List<WalletResponseDTO>> getAllWallets(
      String walletType,
      String status,
      BigDecimal minBalance,
      BigDecimal maxBalance,
      int page,
      int size) {

    RequestContext.setCurrentLayer("SERVICE");
    log.info(
        "\n    ├─ SERVICE ─ getAllWallets\n    │ Filters:\n    │   walletType : {}\n    │   status     : {}\n    │   minBalance : {}\n    │   maxBalance : {}\n    │ Pagination:\n    │   page       : {}\n    │   size       : {}",
        walletType, status, minBalance, maxBalance, page, size);

    // Parse enum filters safely
    Wallet.WalletType walletTypeEnum = parseEnum(walletType, Wallet.WalletType.class, "wallet type");
    Wallet.WalletStatus statusEnum = parseEnum(status, Wallet.WalletStatus.class, "status");

    // Build specification
    Specification<Wallet> spec = Specification
        .where(WalletSpecification.hasWalletType(walletTypeEnum))
        .and(WalletSpecification.hasStatus(statusEnum))
        .and(WalletSpecification.balanceGreaterThanOrEqual(minBalance))
        .and(WalletSpecification.balanceLessThanOrEqual(maxBalance));

    Pageable pageable = PageRequest.of(page, size, Sort.by("walletId").descending());
    Page<Wallet> walletPage = walletRepository.findAll(spec, pageable);

    List<WalletResponseDTO> walletDTOs = walletPage.getContent().stream()
        .map(walletMapper::toDTO)
        .toList();

    log.info(
        "\n    └─ SERVICE ─ getAllWallets\n      Status      : SUCCESS\n      Total Items : {}\n      Total Pages : {}\n      Page        : {} / {}",
        walletPage.getTotalElements(), walletPage.getTotalPages(), page, walletPage.getTotalPages());

    return PaginationResponseDTO.<List<WalletResponseDTO>>builder()
        .totalItems(walletPage.getTotalElements())
        .totalPages(walletPage.getTotalPages())
        .currentPage(page)
        .pageSize(size)
        .data(walletDTOs)
        .build();
  }

  @Override
  public WalletResponseDTO updateWalletStatus(Integer walletId, UpdateWalletStatusRequestDTO request) {
    RequestContext.setCurrentLayer("SERVICE");
    log.info("\n    ├─ SERVICE ─ updateWalletStatus\n    │ Wallet ID  : {}\n    │ New Status : {}", walletId,
        request.getStatus());

    Wallet wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new AppException("Wallet not found with id: " + walletId, HttpStatus.NOT_FOUND));

    Wallet.WalletStatus newStatus = parseEnum(request.getStatus(), Wallet.WalletStatus.class, "status");
    if (newStatus == null) {
      throw new AppException("Status is required", HttpStatus.BAD_REQUEST);
    }

    Wallet.WalletStatus oldStatus = wallet.getStatus();
    wallet.setStatus(newStatus);
    Wallet updated = walletRepository.save(wallet);

    log.info(
        "\n    └─ SERVICE ─ updateWalletStatus\n      Status     : SUCCESS\n      Wallet     : id={}\n      Old Status : {}\n      New Status : {}",
        walletId, oldStatus, newStatus);

    return walletMapper.toDTO(updated);
  }

  /**
   * Safely parse a string to an enum value, returning null if the input is null
   * or blank.
   * Throws AppException if the value is invalid.
   */
  private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass, String fieldName) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Enum.valueOf(enumClass, value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new AppException("Invalid " + fieldName + ": " + value, HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponseDTO<List<TransactionResponseDTO>> getWalletTransactions(
      String email, Integer walletId, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

    RequestContext.setCurrentLayer("SERVICE");
    log.info(
        "\n    \u251c\u2500 SERVICE \u2500 getWalletTransactions\n    \u2502 User     : {}\n    \u2502 Wallet   : {}\n    \u2502 FromDate : {}\n    \u2502 ToDate   : {}",
        email, walletId, fromDate, toDate);

    Wallet wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new AppException("Wallet not found", HttpStatus.NOT_FOUND));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

    // Admin can view any wallet's transactions; non-admin can only view their own
    if (user.getRole() != User.UserRole.ADMIN
        && !wallet.getUser().getUserId().equals(user.getUserId())) {
      throw new AppException("You can only view transactions of your own wallet", HttpStatus.FORBIDDEN);
    }

    Specification<Transaction> spec = Specification
        .where(TransactionSpecification.hasWalletId(walletId))
        .and(TransactionSpecification.createdAfter(fromDate))
        .and(TransactionSpecification.createdBefore(toDate));

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Transaction> txPage = transactionRepository.findAll(spec, pageable);

    List<TransactionResponseDTO> dtos = txPage.getContent().stream()
        .map(transaction -> mapTransactionToDTO(transaction, walletId))
        .toList();

    log.info("\n    \u2514\u2500 SERVICE \u2500 getWalletTransactions\n      Status : SUCCESS\n      Total  : {}",
        txPage.getTotalElements());

    return PaginationResponseDTO.<List<TransactionResponseDTO>>builder()
        .totalItems(txPage.getTotalElements())
        .totalPages(txPage.getTotalPages())
        .currentPage(page)
        .pageSize(size)
        .data(dtos)
        .build();
  }

  private TransactionResponseDTO mapTransactionToDTO(Transaction transaction, Integer currentWalletId) {
    boolean isSender = transaction.getSenderWallet() != null
        && transaction.getSenderWallet().getWalletId().equals(currentWalletId);

    String direction = isSender ? "OUT" : "IN";

    User counterparty;
    if (isSender && transaction.getReceiverWallet() != null) {
      counterparty = transaction.getReceiverWallet().getUser();
    } else if (!isSender && transaction.getSenderWallet() != null) {
      counterparty = transaction.getSenderWallet().getUser();
    } else {
      counterparty = null;
    }

    return new TransactionResponseDTO(
        transaction.getTransactionId(),
        transaction.getTransactionType().name(),
        direction,
        transaction.getAmount(),
        transaction.getCurrency().name(),
        counterparty != null ? counterparty.getFullName() : "System",
        counterparty != null ? counterparty.getEmail() : null,
        transaction.getCreatedAt());
  }

  // ==================== SYSTEM WALLET ====================

  private static final BigDecimal SYSTEM_WALLET_INITIAL_BALANCE = new BigDecimal("100000");

  @Override
  @Transactional
  public void initializeSystemWallet() {
    if (walletRepository.findByWalletType(Wallet.WalletType.SYSTEM).isPresent()) {
      log.info("System wallet already exists, skipping initialization.");
      return;
    }

    Wallet systemWallet = new Wallet();
    systemWallet.setUser(null);
    systemWallet.setWalletType(Wallet.WalletType.SYSTEM);
    systemWallet.setCurrency(Wallet.Currency.BLUE);
    systemWallet.setBalance(SYSTEM_WALLET_INITIAL_BALANCE);
    systemWallet.setStatus(Wallet.WalletStatus.ACTIVE);
    walletRepository.save(systemWallet);

    log.info("System wallet created with initial balance: {}", SYSTEM_WALLET_INITIAL_BALANCE);
  }

  @Override
  @Transactional(readOnly = true)
  public WalletResponseDTO getSystemWallet() {
    Wallet systemWallet = walletRepository.findByWalletType(Wallet.WalletType.SYSTEM)
        .orElseThrow(() -> new AppException("System wallet not found", HttpStatus.NOT_FOUND));
    return walletMapper.toDTO(systemWallet);
  }

  @Override
  @Transactional
  public WalletResponseDTO topUpSystemWallet(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new AppException("Top-up amount must be greater than 0", HttpStatus.BAD_REQUEST);
    }

    Wallet systemWallet = walletRepository.findByWalletType(Wallet.WalletType.SYSTEM)
        .orElseThrow(() -> new AppException("System wallet not found", HttpStatus.NOT_FOUND));

    systemWallet.setBalance(systemWallet.getBalance().add(amount));
    walletRepository.save(systemWallet);

    // Record top-up transaction
    Transaction transaction = new Transaction();
    transaction.setSenderWallet(null);
    transaction.setReceiverWallet(systemWallet);
    transaction.setAmount(amount);
    transaction.setCurrency(Transaction.Currency.BLUE);
    transaction.setTransactionType(Transaction.TransactionType.TOPUP);
    transactionRepository.save(transaction);

    log.info("✅ System wallet topped up: +{} | New balance: {}", amount, systemWallet.getBalance());

    return walletMapper.toDTO(systemWallet);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponseDTO<List<TransactionResponseDTO>> getSystemWalletTransactions(
      LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

    Wallet systemWallet = walletRepository.findByWalletType(Wallet.WalletType.SYSTEM)
        .orElseThrow(() -> new AppException("System wallet not found", HttpStatus.NOT_FOUND));

    Specification<Transaction> spec = Specification
        .where(TransactionSpecification.hasWalletId(systemWallet.getWalletId()))
        .and(TransactionSpecification.createdAfter(fromDate))
        .and(TransactionSpecification.createdBefore(toDate));

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Transaction> txPage = transactionRepository.findAll(spec, pageable);

    List<TransactionResponseDTO> dtos = txPage.getContent().stream()
        .map(tx -> mapTransactionToDTO(tx, systemWallet.getWalletId()))
        .toList();

    return PaginationResponseDTO.<List<TransactionResponseDTO>>builder()
        .totalItems(txPage.getTotalElements())
        .totalPages(txPage.getTotalPages())
        .currentPage(page)
        .pageSize(size)
        .data(dtos)
        .build();
  }
}

