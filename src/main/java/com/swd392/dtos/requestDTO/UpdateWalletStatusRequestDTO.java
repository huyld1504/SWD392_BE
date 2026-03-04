package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWalletStatusRequestDTO {

  @NotNull(message = "Status is required")
  private String status;
}
