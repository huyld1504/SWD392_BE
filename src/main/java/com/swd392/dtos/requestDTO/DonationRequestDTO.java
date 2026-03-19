package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestDTO {

  @NotNull(message = "Article ID is required")
  private Integer articleId;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "1", message = "Minimum donation is 1 coin")
  @DecimalMax(value = "10", message = "Maximum donation is 10 coins per transaction")
  private BigDecimal amount;

  private String message;
}
