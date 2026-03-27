package com.swd392.repositories.specifications;

import com.swd392.entities.FeedingPeriod;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class FeedingPeriodSpecification {

    private FeedingPeriodSpecification() {
    }

    public static Specification<FeedingPeriod> hasStatus(FeedingPeriod.PeriodStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<FeedingPeriod> hasSemesterCode(String semesterCode) {
        return (root, query, cb) -> cb.equal(root.get("semester").get("semesterCode"), semesterCode);
    }

    public static Specification<FeedingPeriod> createdAfter(LocalDateTime fromDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<FeedingPeriod> createdBefore(LocalDateTime toDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
