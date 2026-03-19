package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;

public record WalletResponseDTO(
                Integer walletId,
                String walletType,
                String currency,
                BigDecimal balance,
                String status,
                UserInfoDTO userInfo) {
}
