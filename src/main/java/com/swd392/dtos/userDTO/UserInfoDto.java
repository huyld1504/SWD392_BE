package com.swd392.dtos.userDTO;

import com.swd392.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User information DTO")
public class UserInfoDto {

    @Schema(description = "User's unique identifier", example = "1")
    private Long userId;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's role", example = "STUDENT", allowableValues = {"STUDENT", "LECTURE", "ADMIN"})
    private User.UserRole role;

    @Schema(description = "User's account status", example = "ACTIVE", allowableValues = {"ACTIVE", "SUSPENDED", "BANNED"})
    private User.UserStatus status;

    @Schema(description = "Authentication provider", example = "google", allowableValues = {"local", "google"})
    private String provider;

    @Schema(description = "User's avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Account creation timestamp", example = "2024-01-23T10:30:00")
    private LocalDateTime createdAt;

    public static UserInfoDto fromUser(User user) {
        return new UserInfoDto(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getProvider(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
