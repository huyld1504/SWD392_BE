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
@Tag(name = "Feeding", description = "Semester-based feeding coin management APIs")
public class FeedingController {

    private final FeedingService feedingService;

    // ==================== CREATE ====================

    @Operation(summary = "Create feeding period (Admin)",
        description = "Create a feeding period for a semester. Semester dates are auto-calculated from the code. "
            + "Format: SP26 (Spring, Jan-Apr), SU26 (Summer, May-Aug), FA26 (Fall, Sep-Dec).",
        security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Feeding period created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Period already exists for this semester", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> createFeedingPeriod(
            Authentication authentication,
            @Valid @RequestBody CreateFeedingRequestDTO request) {

        RequestContext.setCurrentLayer("CONTROLLER");
        String email = authentication.getName();
        log.info("\n  ┌─ CONTROLLER ─ createFeedingPeriod\n  │ Admin    : {}\n  │ Semester : {}\n  │ Amount   : {}",
            email, request.getSemesterCode(), request.getGrantAmount());

        FeedingPeriodResponseDTO result = feedingService.createFeedingPeriod(request, email);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<FeedingPeriodResponseDTO>builder()
                .success(true)
                .message("Feeding period created for semester " + request.getSemesterCode())
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    // ==================== UPDATE ====================

    @Operation(summary = "Update feeding period (Admin)",
        description = "Update grantAmount of an ACTIVE feeding period.",
        security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/{periodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> updateFeedingPeriod(
            @PathVariable Integer periodId,
            @Valid @RequestBody UpdateFeedingRequestDTO request) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ updateFeedingPeriod\n  │ PeriodId    : {}\n  │ GrantAmount : {}",
            periodId, request.getGrantAmount());

        FeedingPeriodResponseDTO result = feedingService.updateFeedingPeriod(periodId, request);

        return ResponseEntity.ok(
            ApiResponse.<FeedingPeriodResponseDTO>builder()
                .success(true)
                .message("Feeding period updated successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    // ==================== COMPLETE ====================

    @Operation(summary = "Complete feeding period (Admin)",
        description = "Mark an ACTIVE feeding period as COMPLETED before its natural end date.",
        security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/{periodId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> completeFeedingPeriod(
            @PathVariable Integer periodId) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ completeFeedingPeriod\n  │ PeriodId : {}", periodId);

        FeedingPeriodResponseDTO result = feedingService.completeFeedingPeriod(periodId);

        return ResponseEntity.ok(
            ApiResponse.<FeedingPeriodResponseDTO>builder()
                .success(true)
                .message("Feeding period completed")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    // ==================== CANCEL ====================

    @Operation(summary = "Cancel feeding period (Admin)",
        description = "Cancel an ACTIVE feeding period. Only allowed if no users have been fed yet.",
        security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/{periodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelFeedingPeriod(@PathVariable Integer periodId) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ cancelFeedingPeriod\n  │ PeriodId : {}", periodId);

        feedingService.cancelFeedingPeriod(periodId);

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Feeding period cancelled successfully")
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    // ==================== TRIGGER (Manual) ====================

    @Operation(summary = "Trigger feeding now (Admin)",
        description = "Immediately feed all unfed users for the specified ACTIVE period. "
            + "System wallet must have sufficient balance.",
        security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Feeding triggered"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient system wallet balance", content = @Content)
    })
    @PostMapping("/{periodId}/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedingPeriodResponseDTO>> triggerFeeding(
            @PathVariable Integer periodId) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ triggerFeeding\n  │ PeriodId : {}", periodId);

        FeedingPeriodResponseDTO result = feedingService.triggerFeeding(periodId);

        return ResponseEntity.ok(
            ApiResponse.<FeedingPeriodResponseDTO>builder()
                .success(true)
                .message("Feeding triggered. " + result.getUsersProcessedNow() + " users fed.")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    // ==================== LIST ALL ====================

    @Operation(summary = "Get all feeding periods (Admin)",
        description = "Retrieve a paginated list of feeding periods with optional filters.",
        security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponseDTO<List<FeedingPeriodResponseDTO>>>> getAllFeedings(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by status: ACTIVE, COMPLETED, CANCELLED") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by semester code: SP26, SU26, FA26") @RequestParam(required = false) String semesterCode) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ getAllFeedings\n  │ Page : {}, Size : {}", page, size);

        PaginationResponseDTO<List<FeedingPeriodResponseDTO>> result =
            feedingService.getAllFeedingPeriods(page, size, status, semesterCode);

        return ResponseEntity.ok(
            ApiResponse.<PaginationResponseDTO<List<FeedingPeriodResponseDTO>>>builder()
                .success(true)
                .message("Feeding periods retrieved successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    // ==================== DETAIL ====================

    @Operation(summary = "Get feeding period detail (Admin)",
        description = "Retrieve detailed info including dashboard stats (pendingUsers, deficit, systemBalance) "
            + "and all fed users list.",
        security = @SecurityRequirement(name = "Bearer Authentication"))
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
