package com.swd392.services.interfaces;

import com.swd392.entities.User;
import com.swd392.entities.Wallet;

public interface WalletService {
    Wallet createWalletForUser(User user);
}
