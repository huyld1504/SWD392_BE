package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
    Integer transactionId,
    String transactionType,
    BigDecimal amount,
    String currency,
    String counterpartyName,
    String counterpartyEmail,
    LocalDateTime createdAt) {
}
