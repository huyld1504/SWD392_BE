package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.UpdateWalletStatusRequestDTO;
import com.swd392.dtos.responseDTO.TransactionResponseDTO;
import com.swd392.dtos.responseDTO.WalletResponseDTO;
import com.swd392.entities.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface WalletService {

    /**
     * Create a default MAIN wallet (BLUE currency) for a newly registered user.
     * Called automatically during registration.
     */
    void createDefaultWallet(User user);

    /**
     * Create an EARNED wallet for the currently authenticated user.
     * Each user can only have one EARNED wallet.
     */
    WalletResponseDTO createEarnedWallet(String email);

    /**
     * Get all wallets belonging to the currently authenticated user (via JWT).
     */
    List<WalletResponseDTO> getWalletsByCurrentUser(String email);

    /**
     * Get all wallets with filtering and pagination (Admin only).
     */
    PaginationResponseDTO<List<WalletResponseDTO>> getAllWallets(
            String walletType,
            String status,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            int page,
            int size);

    /**
     * Update the status of a wallet (Admin only).
     */
    WalletResponseDTO updateWalletStatus(Integer walletId, UpdateWalletStatusRequestDTO request);

    /**
     * Get all transactions of a specific wallet (paginated, filterable by date
     * range).
     * Only the wallet owner can view their own transactions.
     */
    PaginationResponseDTO<List<TransactionResponseDTO>> getWalletTransactions(
            String email, Integer walletId, LocalDateTime fromDate, LocalDateTime toDate, int page,
            int size);
}
