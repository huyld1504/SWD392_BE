package com.swd392.dtos.authDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleIdTokenRequest {

  @NotBlank(message = "ID Token is required")
  private String idToken;
}
