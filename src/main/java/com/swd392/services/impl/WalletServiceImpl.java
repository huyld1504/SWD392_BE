package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.UpdateWalletStatusRequestDTO;
import com.swd392.dtos.responseDTO.WalletResponseDTO;
import com.swd392.entities.User;
import com.swd392.entities.Wallet;
import com.swd392.exceptions.AppException;
import com.swd392.mapper.WalletMapper;
import com.swd392.repositories.UserRepository;
import com.swd392.repositories.WalletRepository;
import com.swd392.repositories.specifications.WalletSpecification;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final UserRepository userRepository;
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
    wallet.setBalance(java.math.BigDecimal.ZERO);
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
}
