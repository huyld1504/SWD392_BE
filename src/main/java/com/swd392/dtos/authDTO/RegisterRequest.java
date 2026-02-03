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
    @Max(message = "Password cannot exceed 64 characters", value = 64)
    @Min(message = "Password must be at least 6 characters", value = 6)
  private String password;

    @NotBlank(message = "Full name is required")
    @NotNull(message = "Full name cannot be null")
    @Max(message = "Full name cannot exceed 100 characters", value = 100)
  private String fullName;

    @NotNull(message = "Role cannot be null")
  private User.UserRole role;
}
