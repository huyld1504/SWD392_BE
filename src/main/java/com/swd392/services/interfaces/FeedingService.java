package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.CreateFeedingRequestDTO;
import com.swd392.dtos.requestDTO.UpdateFeedingRequestDTO;
import com.swd392.dtos.responseDTO.FeedingPeriodResponseDTO;

import java.util.List;

public interface FeedingService {

    /**
     * Admin creates a feeding period for a semester.
     */
    FeedingPeriodResponseDTO createFeedingPeriod(CreateFeedingRequestDTO request, String adminEmail);

    /**
     * Admin updates an active feeding period (e.g., grantAmount).
     */
    FeedingPeriodResponseDTO updateFeedingPeriod(Integer periodId, UpdateFeedingRequestDTO request);

    /**
     * Admin completes a feeding period before it naturally ends.
     */
    FeedingPeriodResponseDTO completeFeedingPeriod(Integer periodId);

    /**
     * Admin cancels a feeding period (only if no users have been fed yet).
     */
    void cancelFeedingPeriod(Integer periodId);

    /**
     * Admin triggers feeding manually for a specific period.
     * Feeds all unfed active users immediately.
     */
    FeedingPeriodResponseDTO triggerFeeding(Integer periodId);

    /**
     * Daily scheduler: execute feeding for all active periods.
     * Returns the number of users fed across all periods.
     */
    int executeDailyFeeding();

    /**
     * Get all feeding periods (paginated, with filters).
     */
    PaginationResponseDTO<List<FeedingPeriodResponseDTO>> getAllFeedingPeriods(
        int page, int size, String status, String semesterCode);

    /**
     * Get detailed feeding period info (with stats and user list).
     */
    FeedingPeriodResponseDTO getFeedingDetail(Integer periodId);
}
