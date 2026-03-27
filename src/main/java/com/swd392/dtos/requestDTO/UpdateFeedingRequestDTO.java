package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedingRequestDTO {

    @Positive(message = "Grant amount must be positive")
    private BigDecimal grantAmount;
}
