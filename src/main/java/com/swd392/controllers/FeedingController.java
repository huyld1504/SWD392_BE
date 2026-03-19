package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.CreateFeedingRequestDTO;
import com.swd392.dtos.requestDTO.UpdateFeedingRequestDTO;
import com.swd392.dtos.responseDTO.FeedingPeriodResponseDTO;
import com.swd392.services.interfaces.FeedingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/feedings")
@RequiredArgsConstructor
@Tag(name = "Feeding", description = "Feeding coin reset management APIs")
public class FeedingController {

        private final FeedingService feedingService;

        // ==================== SCHEDULE ====================

        @Operation(summary = "Schedule feeding reset (Admin)", description = "Create a PENDING feeding period scheduled for a future date.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Feeding scheduled"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A PENDING period already exists", content = @Content)
        })
        @PostMapping("/schedule")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> scheduleFeeding(
                        Authentication authentication,
                        @Valid @RequestBody CreateFeedingRequestDTO request) {
                RequestContext.setCurrentLayer("CONTROLLER");
                String email = authentication.getName();
                log.info("\n  ┌─ CONTROLLER ─ scheduleFeeding\n  │ Admin       : {}\n  │ ScheduledAt : {}",
                                email, request.getScheduledAt());

                FeedingPeriodResponseDTO result = feedingService.scheduleFeedingReset(request.getScheduledAt(), email);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<FeedingPeriodResponseDTO>builder()
                                                .success(true)
                                                .message("Feeding period scheduled for " + request.getScheduledAt())
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        // ==================== UPDATE SCHEDULE ====================

        @Operation(summary = "Update feeding schedule (Admin)", description = "Update the scheduledAt date of a PENDING feeding period.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule updated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Period is not PENDING", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Period not found", content = @Content)
        })
        @PutMapping("/{periodId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> updateFeedingSchedule(
                        @PathVariable Integer periodId,
                        @Valid @RequestBody UpdateFeedingRequestDTO request) {
                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("\n  ┌─ CONTROLLER ─ updateFeedingSchedule\n  │ PeriodId    : {}\n  │ NewSchedule : {}",
                                periodId, request.getScheduledAt());

                FeedingPeriodResponseDTO result = feedingService.updateFeedingSchedule(periodId,
                                request.getScheduledAt());

                return ResponseEntity.ok(
                                ApiResponse.<FeedingPeriodResponseDTO>builder()
                                                .success(true)
                                                .message("Feeding schedule updated to " + request.getScheduledAt())
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        // ==================== DELETE FEEDING ====================

        @Operation(summary = "Delete feeding period (Admin)", description = "Delete a PENDING feeding period. Only PENDING periods can be deleted.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Period deleted"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Period is not PENDING", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Period not found", content = @Content)
        })
        @DeleteMapping("/{periodId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteFeedingPeriod(@PathVariable Integer periodId) {
                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("\n  ┌─ CONTROLLER ─ deleteFeedingPeriod\n  │ PeriodId : {}", periodId);

                feedingService.deleteFeedingPeriod(periodId);

                return ResponseEntity.ok(
                                ApiResponse.<Void>builder()
                                                .success(true)
                                                .message("Feeding period deleted successfully")
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        // ==================== TRIGGER NOW ====================

        @Operation(summary = "Trigger feeding reset now (Admin)", description = "Immediately execute a feeding reset for all active users (STUDENT, LECTURE, ADMIN). "
                        + "If a user does not have a MAIN wallet, one will be auto-created. "
                        + "All MAIN wallets are reset to the default grant amount. Once per month.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Feeding completed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already executing or completed this month", content = @Content)
        })
        @PostMapping("/trigger")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> triggerFeeding(Authentication authentication) {
                RequestContext.setCurrentLayer("CONTROLLER");
                String email = authentication.getName();
                log.info("\n  ┌─ CONTROLLER ─ triggerFeeding\n  │ Admin : {}", email);

                FeedingPeriodResponseDTO result = feedingService.executeFeedingReset("MANUAL_ADMIN", email);

                return ResponseEntity.ok(
                                ApiResponse.<FeedingPeriodResponseDTO>builder()
                                                .success(true)
                                                .message("Feeding reset completed. "
                                                                + result.getTotalUsersProcessed()
                                                                + " users processed.")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        // ==================== LIST ALL PERIODS ====================

        @Operation(summary = "Get all feeding periods (Admin)", description = "Retrieve a paginated list of feeding periods with optional filters.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<PaginationResponseDTO<List<FeedingPeriodResponseDTO>>>> getAllFeedings(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Filter from date (ISO: 2026-03-01T00:00:00)") @RequestParam(required = false) LocalDateTime fromDate,
                        @Parameter(description = "Filter to date (ISO: 2026-03-31T23:59:59)") @RequestParam(required = false) LocalDateTime toDate,
                        @Parameter(description = "Filter by status: PENDING, EXECUTING, COMPLETED, FAILED") @RequestParam(required = false) String status,
                        @Parameter(description = "Filter by trigger: MANUAL_ADMIN, AUTO_SCHEDULE") @RequestParam(required = false) String triggerSource) {
                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("\n  ┌─ CONTROLLER ─ getAllFeedings\n  │ Page : {}, Size : {}", page, size);

                PaginationResponseDTO<List<FeedingPeriodResponseDTO>> result = feedingService.getAllFeedingPeriods(
                                page, size, fromDate, toDate, status, triggerSource);

                return ResponseEntity.ok(
                                ApiResponse.<PaginationResponseDTO<List<FeedingPeriodResponseDTO>>>builder()
                                                .success(true)
                                                .message("Feeding periods retrieved successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        // ==================== FEEDING DETAIL ====================

        @Operation(summary = "Get feeding period detail (Admin)", description = "Retrieve detailed info including all users sorted by earned balance (top donated first), with total coins granted and earned summary.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detail retrieved"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Period not found", content = @Content)
        })
        @GetMapping("/{periodId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> getFeedingDetail(
                        @Parameter(description = "Feeding period ID", required = true) @PathVariable Integer periodId) {
                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("\n  ┌─ CONTROLLER ─ getFeedingDetail\n  │ Period : {}", periodId);

                FeedingPeriodResponseDTO result = feedingService.getFeedingDetail(periodId);

                return ResponseEntity.ok(
                                ApiResponse.<FeedingPeriodResponseDTO>builder()
                                                .success(true)
                                                .message("Feeding detail retrieved successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }
}
