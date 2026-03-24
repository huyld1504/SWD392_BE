package com.swd392.repositories.specifications;

import com.swd392.entities.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecification {

  private TransactionSpecification() {
  }

  /**
   * Filter transactions that "belong to" a specific wallet.
   *
   * A transaction belongs to a wallet when:
   * - The wallet is the senderWallet AND the type is a sender-side type
   *   (DONATE, FEEDING, RESET)
   * - OR the wallet is the receiverWallet AND the type is a receiver-side type
   *   (RECEIVE_DONATE, REWARD, TOPUP)
   *
   * This ensures each donation only produces ONE transaction per wallet,
   * with the correct direction (OUT for sender, IN for receiver).
   */
  public static Specification<Transaction> belongsToWallet(Integer walletId) {
    return (root, query, cb) -> {
      if (walletId == null) return null;

      // Sender-side types: transaction belongs to the senderWallet
      var isSenderRecord = cb.and(
          cb.equal(root.get("senderWallet").get("walletId"), walletId),
          root.get("transactionType").in(
              Transaction.TransactionType.DONATE,
              Transaction.TransactionType.FEEDING,
              Transaction.TransactionType.RESET
          )
      );

      // Receiver-side types: transaction belongs to the receiverWallet
      var isReceiverRecord = cb.and(
          cb.equal(root.get("receiverWallet").get("walletId"), walletId),
          root.get("transactionType").in(
              Transaction.TransactionType.RECEIVE_DONATE,
              Transaction.TransactionType.REWARD,
              Transaction.TransactionType.TOPUP,
              Transaction.TransactionType.FEEDING    // User cũng thấy FEEDING (nhận coin)
          )
      );

      return cb.or(isSenderRecord, isReceiverRecord);
    };
  }

  /**
   * @deprecated Use {@link #belongsToWallet(Integer)} instead for correct
   *             ownership-based filtering.
   */
  @Deprecated
  public static Specification<Transaction> hasWalletId(Integer walletId) {
    return (root, query, cb) -> walletId == null ? null
        : cb.or(
            cb.equal(root.get("senderWallet").get("walletId"), walletId),
            cb.equal(root.get("receiverWallet").get("walletId"), walletId));
  }

  public static Specification<Transaction> createdAfter(LocalDateTime fromDate) {
    return (root, query, cb) -> fromDate == null ? null
        : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
  }

  public static Specification<Transaction> createdBefore(LocalDateTime toDate) {
    return (root, query, cb) -> toDate == null ? null
        : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
  }

  /**
   * Filter transactions by semester code (e.g., "SP26").
   */
  public static Specification<Transaction> hasSemesterCode(String semesterCode) {
    return (root, query, cb) -> semesterCode == null || semesterCode.isBlank() ? null
        : cb.equal(root.get("semester").get("semesterCode"), semesterCode.toUpperCase());
  }
}
