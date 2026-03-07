package com.swd392.repositories;

import com.swd392.entities.FeedingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedingPeriodRepository
    extends JpaRepository<FeedingPeriod, Integer>, JpaSpecificationExecutor<FeedingPeriod> {

  /**
   * Check if there's a feeding period with a specific status.
   */
  boolean existsByStatus(FeedingPeriod.PeriodStatus status);

  /**
   * Find completed periods within a date range (check if a month already ran).
   */
  List<FeedingPeriod> findByStatusAndExecutedAtBetween(
      FeedingPeriod.PeriodStatus status, LocalDateTime from, LocalDateTime to);

  /**
   * Find PENDING periods scheduled for today or earlier (ready to execute).
   */
  List<FeedingPeriod> findByStatusAndScheduledAtLessThanEqual(
      FeedingPeriod.PeriodStatus status, LocalDateTime dateTime);

  /**
   * Find all periods ordered by created_at descending (for admin listing).
   */
  List<FeedingPeriod> findAllByOrderByCreatedAtDesc();
}
