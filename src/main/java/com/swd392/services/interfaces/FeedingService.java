package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.responseDTO.FeedingPeriodResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedingService {

  /**
   * Admin creates a PENDING feeding period scheduled for a future date.
   */
  FeedingPeriodResponseDTO scheduleFeedingReset(LocalDateTime scheduledAt, String adminEmail);

  /**
   * Admin updates the scheduledAt of a PENDING feeding period.
   */
  FeedingPeriodResponseDTO updateFeedingSchedule(Integer periodId, LocalDateTime newScheduledAt);

  /**
   * Admin deletes a PENDING feeding period.
   */
  void deleteFeedingPeriod(Integer periodId);

  /**
   * Execute feeding reset immediately.
   */
  FeedingPeriodResponseDTO executeFeedingReset(String triggerSource, String adminEmail);

  /**
   * Execute a specific PENDING feeding period (used by daily scheduler).
   */
  FeedingPeriodResponseDTO executePendingPeriod(Integer periodId);

  /**
   * Get all feeding periods (paginated, filterable by date range, status,
   * triggerSource).
   */
  PaginationResponseDTO<List<FeedingPeriodResponseDTO>> getAllFeedingPeriods(
      int page, int size, LocalDateTime fromDate, LocalDateTime toDate, String status, String triggerSource);

  /**
   * Get feeding period detail with list of students sorted by
   * snapshotEarnedBalance DESC.
   */
  FeedingPeriodResponseDTO getFeedingDetail(Integer periodId);
}
