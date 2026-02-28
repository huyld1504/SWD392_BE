package com.swd392.services.impl;

import com.swd392.entities.User;
import com.swd392.entities.Wallet;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.WalletRepository;
import com.swd392.services.interfaces.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public Wallet createWalletForUser(User user){
        if (walletRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new AppException("Wallet already exists");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setWalletType(Wallet.WalletType.EARNED);
        wallet.setCurrency(Wallet.Currency.BLUE);
        wallet.setStatus(Wallet.WalletStatus.ACTIVE);

        return walletRepository.save(wallet);
    }

}
