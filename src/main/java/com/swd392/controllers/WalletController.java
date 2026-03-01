package com.swd392.controllers;

import com.swd392.dtos.responseDTO.WalletResponse;
import com.swd392.entities.Wallet;
import com.swd392.services.interfaces.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        Wallet wallet = walletService.getWalletByEmail(email);

        WalletResponse response = WalletResponse.builder()
                .walletId(Long.valueOf(wallet.getWalletId()))
                .userId(wallet.getUser().getUserId())
                .balance(wallet.getBalance())
                .build();

        return ResponseEntity.ok(response);
}
    @PreAuthorize( "hasRole('ADMIN')")
    @GetMapping("/admin/getAll")
    public ResponseEntity<List<WalletResponse>> getAllWallets() {

        List<Wallet> wallets = walletService.getWalletList();

        List<WalletResponse> response = wallets.stream()
                .map(wallet -> WalletResponse.builder()
                        .walletId(Long.valueOf(wallet.getWalletId()))
                        .userId(wallet.getUser().getUserId())
                        .balance(wallet.getBalance())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }
}
