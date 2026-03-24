package com.swd392.dtos.responseDTO;

import java.math.BigDecimal;

public record FeedingStatsDTO(
    Integer totalUsersFed,
    BigDecimal totalCoinsFed,
    Integer pendingUsers,
    BigDecimal estimatedCoinsNeeded,
    BigDecimal systemWalletBalance,
    BigDecimal deficit
) {}
