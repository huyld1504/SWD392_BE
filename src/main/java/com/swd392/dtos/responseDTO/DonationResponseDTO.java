package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationResponseDTO(
    Integer donationId,
    UserInfoDTO sender,
    UserInfoDTO receiver,
    ArticleInfoDTO article,
    BigDecimal amount,
    String currency,
    String message,
    LocalDateTime createdAt) {
}
