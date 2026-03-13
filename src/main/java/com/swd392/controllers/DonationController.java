package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.DonationRequestDTO;
import com.swd392.dtos.responseDTO.DonationResponseDTO;
import com.swd392.services.interfaces.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
@Tag(name = "Donation", description = "Donation management APIs - donate BLUE coins to article authors")
public class DonationController {

  private final DonationService donationService;

  /**
   * Donate BLUE coins to an article's author.
   */
  @Operation(summary = "Donate to an article", description = "Donate BLUE coins (1-10 per transaction) to an article's author. "
      + "Both sender and receiver use MAIN wallet. "
      + "Article must be APPROVED. Cannot donate to your own article. "
      + "All authenticated users (STUDENT, LECTURE, ADMIN) can donate.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Donation successful", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request (self-donate, article not approved, insufficient balance)", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Sender or receiver wallet is locked", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Article or user not found", content = @Content)
  })
  @PostMapping
  @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURE') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<DonationResponseDTO>> donate(
      Authentication authentication,
      @Valid @RequestBody DonationRequestDTO request) {

    RequestContext.setCurrentLayer("CONTROLLER");
    String email = authentication.getName();
    log.info("\n  ┌─ CONTROLLER ─ donate\n  │ User    : {}\n  │ Article : {}\n  │ Amount  : {}",
        email, request.getArticleId(), request.getAmount());

    DonationResponseDTO result = donationService.donate(email, request);

    log.info("\n  └─ CONTROLLER ─ donate\n    Status : SUCCESS\n    ID     : {}", result.donationId());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.<DonationResponseDTO>builder()
            .success(true)
            .message("Donation successful")
            .data(result)
            .requestId(RequestContext.getRequestId())
            .timestamp(LocalDateTime.now().toString())
            .build());
  }

  /**
   * Get all donations for a specific article (paginated).
   */
  @Operation(summary = "Get donations by article", description = "Retrieve all donations for a specific article with pagination, ordered by newest first.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donations retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Article not found", content = @Content)
  })
  @GetMapping("/article/{articleId}")
  @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURE') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<PaginationResponseDTO<List<DonationResponseDTO>>>> getDonationsByArticle(
      @Parameter(description = "ID of the article", required = true, example = "1") @PathVariable Integer articleId,
      @Parameter(description = "Filter from date (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00") @RequestParam(required = false) LocalDateTime fromDate,
      @Parameter(description = "Filter to date (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-12-31T23:59:59") @RequestParam(required = false) LocalDateTime toDate,
      @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

    RequestContext.setCurrentLayer("CONTROLLER");
    log.info("\n  ┌─ CONTROLLER ─ getDonationsByArticle\n  │ Article  : {}\n  │ FromDate : {}\n  │ ToDate   : {}",
        articleId, fromDate, toDate);

    PaginationResponseDTO<List<DonationResponseDTO>> result = donationService.getDonationsByArticle(
        articleId, fromDate, toDate, page, size);

    log.info("\n  └─ CONTROLLER ─ getDonationsByArticle\n    Status : SUCCESS\n    Total  : {}",
        result.getTotalItems());

    return ResponseEntity.ok(
        ApiResponse.<PaginationResponseDTO<List<DonationResponseDTO>>>builder()
            .success(true)
            .message("Donations retrieved successfully")
            .data(result)
            .requestId(RequestContext.getRequestId())
            .timestamp(LocalDateTime.now().toString())
            .build());
  }

  /**
   * Get all donations sent by the currently authenticated user (paginated).
   */
  @Operation(summary = "Get my donation history", description = "Retrieve all donations sent by the currently authenticated user with pagination, ordered by newest first.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donation history retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @GetMapping("/me")
  @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURE') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<PaginationResponseDTO<List<DonationResponseDTO>>>> getMyDonations(
      Authentication authentication,
      @Parameter(description = "Filter from date (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00") @RequestParam(required = false) LocalDateTime fromDate,
      @Parameter(description = "Filter to date (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-12-31T23:59:59") @RequestParam(required = false) LocalDateTime toDate,
      @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

    RequestContext.setCurrentLayer("CONTROLLER");
    String email = authentication.getName();
    log.info("\n  ┌─ CONTROLLER ─ getMyDonations\n  │ User     : {}\n  │ FromDate : {}\n  │ ToDate   : {}",
        email, fromDate, toDate);

    PaginationResponseDTO<List<DonationResponseDTO>> result = donationService.getMyDonations(
        email, fromDate, toDate, page, size);

    log.info("\n  └─ CONTROLLER ─ getMyDonations\n    Status : SUCCESS\n    Total  : {}", result.getTotalItems());

    return ResponseEntity.ok(
        ApiResponse.<PaginationResponseDTO<List<DonationResponseDTO>>>builder()
            .success(true)
            .message("Donation history retrieved successfully")
            .data(result)
            .requestId(RequestContext.getRequestId())
            .timestamp(LocalDateTime.now().toString())
            .build());
  }
}
