package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        Integer transactionId,
        String transactionType,
        String direction,
        BigDecimal amount,
        String currency,
        UserInfoDTO sender,
        UserInfoDTO receiver,
        LocalDateTime createdAt) {
}
