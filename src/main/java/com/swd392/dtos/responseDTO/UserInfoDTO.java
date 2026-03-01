package com.swd392.dtos.responseDTO;

public record UserInfoDTO(
    Long userId,
    String name,
    String email,
    String avatarUrl) {
}
