package com.swd392.services.impl;

import com.swd392.entities.User;
import com.swd392.entities.Wallet;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.UserRepository;
import com.swd392.repositories.WalletRepository;
import com.swd392.services.interfaces.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

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

    //User view wallet
    public Wallet getWalletByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found"));

        return walletRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new AppException("Wallet not found"));
    }

    //Admin view wallet
    public Wallet getWalletById(Long walletId){
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new AppException("Wallet not found with id: " + walletId));
    }

    //Admin view wallet
    public Wallet getWalletByUserId(Long userId){
        return walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException("Wallet not found with userId: " + userId));
    }

    //Admin all view wallet
    public List<Wallet> getWalletList(){
        return walletRepository.findAll();
    }
    @Transactional
    public Wallet deposit(Long walletId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Amount must be positive");
        }

        Wallet wallet = getWalletById(walletId);

        wallet.setBalance(wallet.getBalance().add(amount));

        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet withdraw(Long walletId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Amount must be positive");
        }

        Wallet wallet = getWalletById(walletId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new AppException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        return walletRepository.save(wallet);
    }



}
