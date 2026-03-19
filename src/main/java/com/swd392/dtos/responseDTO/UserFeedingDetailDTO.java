package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;

public record UserFeedingDetailDTO(
    Integer feedingId,
    UserInfoDTO user,
    BigDecimal amountReceived,
    BigDecimal snapshotEarnedBalance) {
}
