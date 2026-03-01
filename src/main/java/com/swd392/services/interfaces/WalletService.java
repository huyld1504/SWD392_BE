package com.swd392.services.interfaces;

import com.swd392.entities.User;
import com.swd392.entities.Wallet;

import java.util.List;

public interface WalletService {
    Wallet createWalletForUser(User user);
    Wallet getWalletByEmail(String email);
    List<Wallet> getWalletList();
    Wallet getWalletById(Long walletId);
    Wallet getWalletByUserId(Long userId);

}
