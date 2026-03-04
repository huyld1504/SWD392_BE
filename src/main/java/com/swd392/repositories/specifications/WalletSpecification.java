package com.swd392.repositories.specifications;

import com.swd392.entities.Wallet;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class WalletSpecification {

  private WalletSpecification() {
  }

  public static Specification<Wallet> hasWalletType(Wallet.WalletType walletType) {
    return (root, query, criteriaBuilder) -> walletType == null ? null
        : criteriaBuilder.equal(root.get("walletType"), walletType);
  }

  public static Specification<Wallet> hasStatus(Wallet.WalletStatus status) {
    return (root, query, criteriaBuilder) -> status == null ? null : criteriaBuilder.equal(root.get("status"), status);
  }

  public static Specification<Wallet> balanceGreaterThanOrEqual(BigDecimal minBalance) {
    return (root, query, criteriaBuilder) -> minBalance == null ? null
        : criteriaBuilder.greaterThanOrEqualTo(root.get("balance"), minBalance);
  }

  public static Specification<Wallet> balanceLessThanOrEqual(BigDecimal maxBalance) {
    return (root, query, criteriaBuilder) -> maxBalance == null ? null
        : criteriaBuilder.lessThanOrEqualTo(root.get("balance"), maxBalance);
  }
}
