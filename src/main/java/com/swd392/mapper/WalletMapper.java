package com.swd392.mapper;

import com.swd392.dtos.responseDTO.UserInfoDTO;
import com.swd392.dtos.responseDTO.WalletResponseDTO;
import com.swd392.entities.User;
import com.swd392.entities.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

  public WalletResponseDTO toDTO(Wallet wallet) {
    User user = wallet.getUser();

    UserInfoDTO userInfoDTO = null;
    if (user != null) {
      userInfoDTO = new UserInfoDTO(
          user.getUserId(),
          user.getFullName(),
          user.getEmail(),
          user.getAvatarUrl());
    }

    return new WalletResponseDTO(
        wallet.getWalletId(),
        wallet.getWalletType().name(),
        wallet.getCurrency().name(),
        wallet.getBalance(),
        wallet.getStatus().name(),
        userInfoDTO);
  }
}
