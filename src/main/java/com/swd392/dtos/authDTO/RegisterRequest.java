package com.swd392.dtos.authDTO;

import com.swd392.entities.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
  private String email;
  private String password;
  private String fullName;
  private User.UserRole role;
}
