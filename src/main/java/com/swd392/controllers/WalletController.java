package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.UpdateWalletStatusRequestDTO;
import com.swd392.dtos.responseDTO.WalletResponseDTO;
import com.swd392.services.interfaces.WalletService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management APIs")
public class WalletController {

        private final WalletService walletService;

        /**
         * Create an EARNED wallet for the currently authenticated user.
         */
        @Operation(summary = "Create EARNED wallet", description = "Create a new EARNED wallet (GOLD currency) for the currently authenticated user. Each user can only have one EARNED wallet.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "EARNED wallet created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already has an EARNED wallet", content = @Content)
        })
        @PostMapping("/earned")
        public ResponseEntity<ApiResponse<WalletResponseDTO>> createEarnedWallet(Authentication authentication) {
                RequestContext.setCurrentLayer("CONTROLLER");
                String email = authentication.getName();
                log.info("\n  ┌─ CONTROLLER ─ createEarnedWallet\n  │ User : {}", email);

                WalletResponseDTO result = walletService.createEarnedWallet(email);

                log.info("\n  └─ CONTROLLER ─ createEarnedWallet\n    Status  : SUCCESS\n    Wallet  : id={}, type={}",
                                result.walletId(), result.walletType());

                return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                                .body(ApiResponse.<WalletResponseDTO>builder()
                                                .success(true)
                                                .message("EARNED wallet created successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        /**
         * Get all wallets of the currently authenticated user (via JWT).
         */
        @Operation(summary = "Get my wallets", description = "Retrieve all wallets belonging to the currently authenticated user. Requires valid JWT token.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallets retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
        })
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<List<WalletResponseDTO>>> getMyWallets(Authentication authentication) {
                RequestContext.setCurrentLayer("CONTROLLER");
                String email = authentication.getName();
                log.info("\n  ┌─ CONTROLLER ─ getMyWallets\n  │ User : {}", email);

                List<WalletResponseDTO> wallets = walletService.getWalletsByCurrentUser(email);

                log.info("\n  └─ CONTROLLER ─ getMyWallets\n    Status  : SUCCESS\n    Count   : {} wallet(s)",
                                wallets.size());

                return ResponseEntity.ok(
                                ApiResponse.<List<WalletResponseDTO>>builder()
                                                .success(true)
                                                .message("Wallets retrieved successfully")
                                                .data(wallets)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        /**
         * Get all wallets with filtering and pagination (Admin only).
         */
        @Operation(summary = "Get all wallets (Admin)", description = "Retrieve all wallets with optional filtering by wallet type, status, and balance range. Supports pagination. Admin access only.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All wallets retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters (e.g., invalid wallet type or status)", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content)
        })
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<PaginationResponseDTO<List<WalletResponseDTO>>>> getAllWallets(
                        @Parameter(description = "Filter by wallet type (MAIN, EARNED)", example = "MAIN") @RequestParam(required = false) String walletType,

                        @Parameter(description = "Filter by wallet status (ACTIVE, LOCKED)", example = "ACTIVE") @RequestParam(required = false) String status,

                        @Parameter(description = "Minimum balance filter", example = "0") @RequestParam(required = false) BigDecimal minBalance,

                        @Parameter(description = "Maximum balance filter", example = "10000") @RequestParam(required = false) BigDecimal maxBalance,

                        @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

                RequestContext.setCurrentLayer("CONTROLLER");
                log.info(
                                "\n  ┌─ CONTROLLER ─ getAllWallets (Admin)\n  │ Filters:\n  │   walletType  : {}\n  │   status      : {}\n  │   minBalance  : {}\n  │   maxBalance  : {}\n  │ Pagination:\n  │   page        : {}\n  │   size        : {}",
                                walletType, status, minBalance, maxBalance, page, size);

                PaginationResponseDTO<List<WalletResponseDTO>> result = walletService.getAllWallets(walletType, status,
                                minBalance,
                                maxBalance, page, size);

                log.info(
                                "\n  └─ CONTROLLER ─ getAllWallets\n    Status      : SUCCESS\n    Total Items : {}\n    Total Pages : {}\n    Page        : {} / {}",
                                result.getTotalItems(), result.getTotalPages(), result.getCurrentPage(),
                                result.getTotalPages());

                return ResponseEntity.ok(
                                ApiResponse.<PaginationResponseDTO<List<WalletResponseDTO>>>builder()
                                                .success(true)
                                                .message("All wallets retrieved successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        /**
         * Update wallet status (Admin only).
         */
        @Operation(summary = "Update wallet status (Admin)", description = "Update the status of a specific wallet. Admin can set status to ACTIVE or LOCKED. Admin access only.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet status updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content)
        })
        @PutMapping("/{walletId}/status")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<WalletResponseDTO>> updateWalletStatus(
                        @Parameter(description = "ID of the wallet to update", required = true, example = "1") @PathVariable Integer walletId,
                        @Valid @RequestBody UpdateWalletStatusRequestDTO request) {

                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("\n  ┌─ CONTROLLER ─ updateWalletStatus (Admin)\n  │ Wallet ID  : {}\n  │ New Status : {}",
                                walletId,
                                request.getStatus());

                WalletResponseDTO result = walletService.updateWalletStatus(walletId, request);

                log.info("\n  └─ CONTROLLER ─ updateWalletStatus\n    Status  : SUCCESS\n    Wallet  : id={}, status={}",
                                walletId, result.status());

                return ResponseEntity.ok(
                                ApiResponse.<WalletResponseDTO>builder()
                                                .success(true)
                                                .message("Wallet status updated successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        /**
         * Get all transactions of a specific wallet (paginated).
         */
        @Operation(summary = "Get wallet transactions", description = "Retrieve all transactions of a specific wallet with pagination, ordered by newest first. Only the wallet owner can view.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the wallet owner", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content)
        })
        @GetMapping("/{walletId}/transactions")
        public ResponseEntity<ApiResponse<PaginationResponseDTO<List<com.swd392.dtos.responseDTO.TransactionResponseDTO>>>> getWalletTransactions(
                        Authentication authentication,
                        @Parameter(description = "ID of the wallet", required = true, example = "1") @PathVariable Integer walletId,
                        @Parameter(description = "Filter from date (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00") @RequestParam(required = false) LocalDateTime fromDate,
                        @Parameter(description = "Filter to date (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-12-31T23:59:59") @RequestParam(required = false) LocalDateTime toDate,
                        @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

                RequestContext.setCurrentLayer("CONTROLLER");
                String email = authentication.getName();
                log.info("\n  \u250c\u2500 CONTROLLER \u2500 getWalletTransactions\n  \u2502 User     : {}\n  \u2502 Wallet   : {}\n  \u2502 FromDate : {}\n  \u2502 ToDate   : {}",
                                email, walletId, fromDate, toDate);

                PaginationResponseDTO<List<com.swd392.dtos.responseDTO.TransactionResponseDTO>> result = walletService
                                .getWalletTransactions(email, walletId, fromDate, toDate, page, size);

                log.info("\n  \u2514\u2500 CONTROLLER \u2500 getWalletTransactions\n    Status : SUCCESS\n    Total  : {}",
                                result.getTotalItems());

                return ResponseEntity.ok(
                                ApiResponse.<PaginationResponseDTO<List<com.swd392.dtos.responseDTO.TransactionResponseDTO>>>builder()
                                                .success(true)
                                                .message("Transactions retrieved successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }
}
