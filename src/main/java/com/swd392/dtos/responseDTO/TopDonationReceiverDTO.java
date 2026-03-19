package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;

public record TopDonationReceiverDTO(
    Long userId,
    String fullName,
    String email,
    String avatarUrl,
    BigDecimal totalReceived,
    long totalDonations) {
}
