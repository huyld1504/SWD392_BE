package com.swd392.dtos.authDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Login request with email and password credentials")
public class LoginRequest {

    @Schema(description = "User's email address", example = "user@example.com")
    @Email(message = "Email is not valid")
    @NotNull(message = "Email cannot be empty")
    private String email;

    @Schema(description = "User's password", example = "password123")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @NotNull(message = "Password cannot be empty")
    private String password;
}
