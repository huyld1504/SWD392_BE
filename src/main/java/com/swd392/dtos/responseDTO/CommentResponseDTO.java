package com.swd392.dtos.responseDTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponseDTO {

    private Integer commentId;

    private UserInfoDTO user;

    private String content;

    private Integer ratingStar;

    private Boolean isPinned;

    private LocalDateTime createdAt;

    private List<CommentResponseDTO> replies;
}