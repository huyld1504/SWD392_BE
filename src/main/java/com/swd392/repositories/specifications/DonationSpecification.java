package com.swd392.repositories.specifications;

import com.swd392.entities.Donation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class DonationSpecification {

  private DonationSpecification() {
  }

  public static Specification<Donation> hasSenderId(Long userId) {
    return (root, query, cb) -> userId == null ? null
        : cb.equal(root.get("sender").get("userId"), userId);
  }

  public static Specification<Donation> hasArticleId(Integer articleId) {
    return (root, query, cb) -> articleId == null ? null
        : cb.equal(root.get("article").get("articleId"), articleId);
  }

  public static Specification<Donation> createdAfter(LocalDateTime fromDate) {
    return (root, query, cb) -> fromDate == null ? null
        : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
  }

  public static Specification<Donation> createdBefore(LocalDateTime toDate) {
    return (root, query, cb) -> toDate == null ? null
        : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
  }
}
