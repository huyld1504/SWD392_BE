package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.DonationRequestDTO;
import com.swd392.dtos.responseDTO.DonationResponseDTO;
import com.swd392.entities.*;
import com.swd392.exceptions.AppException;
import com.swd392.mapper.DonationMapper;
import com.swd392.repositories.*;
import com.swd392.repositories.specifications.DonationSpecification;
import com.swd392.services.interfaces.DonationService;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationServiceImpl implements DonationService {

  private final DonationRepository donationRepository;
  private final TransactionRepository transactionRepository;
  private final WalletRepository walletRepository;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final DonationMapper donationMapper;

  @Override
  @Transactional
  public DonationResponseDTO donate(String senderEmail, DonationRequestDTO request) {
    RequestContext.setCurrentLayer("SERVICE");
    log.info("\n    ├─ SERVICE ─ donate\n    │ Sender  : {}\n    │ Article : {}\n    │ Amount  : {}",
        senderEmail, request.getArticleId(), request.getAmount());

    // 1. Find sender
    User sender = userRepository.findByEmail(senderEmail)
        .orElseThrow(() -> new AppException("Sender not found", HttpStatus.NOT_FOUND));

    // 2. Find article & validate status
    Article article = articleRepository.findById(request.getArticleId())
        .orElseThrow(() -> new AppException("Article not found", HttpStatus.NOT_FOUND));

    if (article.getStatus() != Article.ArticleStatus.APPROVED) {
      throw new AppException("Can only donate to approved articles", HttpStatus.BAD_REQUEST);
    }

    // 3. Receiver = article author, cannot donate to yourself
    User receiver = article.getAuthor();
    if (sender.getUserId().equals(receiver.getUserId())) {
      throw new AppException("Cannot donate to your own article", HttpStatus.BAD_REQUEST);
    }

    // 4. Find MAIN wallets for both sender and receiver
    Wallet senderWallet = walletRepository.findByUserAndWalletType(sender, Wallet.WalletType.MAIN)
        .orElseThrow(() -> new AppException("Sender does not have a MAIN wallet", HttpStatus.BAD_REQUEST));

    Wallet receiverWallet = walletRepository.findByUserAndWalletType(receiver, Wallet.WalletType.MAIN)
        .orElseThrow(() -> new AppException("Receiver does not have a MAIN wallet", HttpStatus.BAD_REQUEST));

    // 5. Validate wallet status
    if (senderWallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
      throw new AppException("Sender wallet is locked", HttpStatus.FORBIDDEN);
    }
    if (receiverWallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
      throw new AppException("Receiver wallet is locked", HttpStatus.FORBIDDEN);
    }

    // 6. Check sender balance
    if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
      throw new AppException("Insufficient balance. Current: " + senderWallet.getBalance(), HttpStatus.BAD_REQUEST);
    }

    // 7. Transfer: debit sender, credit receiver
    senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
    receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));
    walletRepository.save(senderWallet);
    walletRepository.save(receiverWallet);

    // 8. Create double-entry Transaction records
    // Transaction #1: Sender's DONATE record (money going out)
    Transaction senderTransaction = new Transaction();
    senderTransaction.setSenderWallet(senderWallet);
    senderTransaction.setReceiverWallet(receiverWallet);
    senderTransaction.setAmount(request.getAmount());
    senderTransaction.setCurrency(Transaction.Currency.BLUE);
    senderTransaction.setTransactionType(Transaction.TransactionType.DONATE);
    transactionRepository.save(senderTransaction);

    // Transaction #2: Receiver's RECEIVE_DONATE record (money coming in)
    Transaction receiverTransaction = new Transaction();
    receiverTransaction.setSenderWallet(senderWallet);
    receiverTransaction.setReceiverWallet(receiverWallet);
    receiverTransaction.setAmount(request.getAmount());
    receiverTransaction.setCurrency(Transaction.Currency.BLUE);
    receiverTransaction.setTransactionType(Transaction.TransactionType.RECEIVE_DONATE);
    transactionRepository.save(receiverTransaction);

    // 9. Create Donation record (linked to sender's transaction)
    Donation donation = new Donation();
    donation.setSender(sender);
    donation.setArticle(article);
    donation.setTransaction(senderTransaction);
    donation.setCurrency(Donation.Currency.BLUE);
    donation.setAmount(request.getAmount());
    donation.setMessage(request.getMessage());
    donationRepository.save(donation);

    log.info(
        "\n    └─ SERVICE ─ donate\n      Status       : SUCCESS\n      Donation     : id={}\n      Tx DONATE    : id={}\n      Tx RECEIVE   : id={}\n      Sender       : {} (balance: {})\n      Receiver     : {} (balance: {})\n      Amount       : {} BLUE",
        donation.getDonationId(), senderTransaction.getTransactionId(), receiverTransaction.getTransactionId(),
        senderEmail, senderWallet.getBalance(),
        receiver.getEmail(), receiverWallet.getBalance(), request.getAmount());

    return donationMapper.toDTO(donation);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponseDTO<List<DonationResponseDTO>> getDonationsByArticle(
      Integer articleId, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

    RequestContext.setCurrentLayer("SERVICE");
    log.info(
        "\n    ├─ SERVICE ─ getDonationsByArticle\n    │ Article  : {}\n    │ FromDate : {}\n    │ ToDate   : {}\n    │ Page     : {}",
        articleId, fromDate, toDate, page);

    if (!articleRepository.existsById(articleId)) {
      throw new AppException("Article not found", HttpStatus.NOT_FOUND);
    }

    Specification<Donation> spec = Specification
        .where(DonationSpecification.hasArticleId(articleId))
        .and(DonationSpecification.createdAfter(fromDate))
        .and(DonationSpecification.createdBefore(toDate));

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Donation> donationPage = donationRepository.findAll(spec, pageable);

    List<DonationResponseDTO> dtos = donationPage.getContent().stream()
        .map(donationMapper::toDTO)
        .toList();

    log.info(
        "\n    └─ SERVICE ─ getDonationsByArticle\n      Status : SUCCESS\n      Total  : {}\n      Page   : {} / {}",
        donationPage.getTotalElements(), page, donationPage.getTotalPages());

    return PaginationResponseDTO.<List<DonationResponseDTO>>builder()
        .totalItems(donationPage.getTotalElements())
        .totalPages(donationPage.getTotalPages())
        .currentPage(page)
        .pageSize(size)
        .data(dtos)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponseDTO<List<DonationResponseDTO>> getMyDonations(
      String email, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

    RequestContext.setCurrentLayer("SERVICE");
    log.info(
        "\n    ├─ SERVICE ─ getMyDonations\n    │ User     : {}\n    │ FromDate : {}\n    │ ToDate   : {}\n    │ Page     : {}",
        email, fromDate, toDate, page);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

    Specification<Donation> spec = Specification
        .where(DonationSpecification.hasSenderId(user.getUserId()))
        .and(DonationSpecification.createdAfter(fromDate))
        .and(DonationSpecification.createdBefore(toDate));

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Donation> donationPage = donationRepository.findAll(spec, pageable);

    List<DonationResponseDTO> dtos = donationPage.getContent().stream()
        .map(donationMapper::toDTO)
        .toList();

    log.info("\n    └─ SERVICE ─ getMyDonations\n      Status : SUCCESS\n      Total  : {}\n      Page   : {} / {}",
        donationPage.getTotalElements(), page, donationPage.getTotalPages());

    return PaginationResponseDTO.<List<DonationResponseDTO>>builder()
        .totalItems(donationPage.getTotalElements())
        .totalPages(donationPage.getTotalPages())
        .currentPage(page)
        .pageSize(size)
        .data(dtos)
        .build();
  }
}
