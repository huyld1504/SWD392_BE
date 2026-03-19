package com.swd392.dtos.authDTO;

import com.swd392.entities.User;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @Email(message = "Invalid email format")
  @NotBlank(message = "Email is required")
  @NotNull(message = "Email cannot be null")
  private String email;

  @NotBlank(message = "Password is required")
  @NotNull(message = "Password cannot be null")
  @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
  private String password;

  @NotBlank(message = "Full name is required")
  @NotNull(message = "Full name cannot be null")
  @Size(max = 100, message = "Full name cannot exceed 100 characters")
  private String fullName;

  @NotNull(message = "Role cannot be null")
  private User.UserRole role;
}
