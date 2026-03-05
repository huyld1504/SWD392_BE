package com.swd392.repositories.specifications;

import com.swd392.entities.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecification {

  private TransactionSpecification() {
  }

  /**
   * Filter transactions where the wallet is either sender or receiver.
   */
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
}
