package com.swd392.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.swd392.entities.User;
import com.swd392.entities.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer>, JpaSpecificationExecutor<Wallet> {

  List<Wallet> findAllByUserUserId(Long userId);

  boolean existsByUserAndWalletType(User user, Wallet.WalletType walletType);

  Optional<Wallet> findByUserAndWalletType(User user, Wallet.WalletType walletType);

  Optional<Wallet> findByWalletType(Wallet.WalletType walletType);
}
