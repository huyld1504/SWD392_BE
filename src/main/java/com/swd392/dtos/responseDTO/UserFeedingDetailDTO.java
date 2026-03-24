package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserFeedingDetailDTO(
    Integer feedingId,
    UserInfoDTO user,
    BigDecimal amountReceived,
    LocalDateTime fedAt) {
}
