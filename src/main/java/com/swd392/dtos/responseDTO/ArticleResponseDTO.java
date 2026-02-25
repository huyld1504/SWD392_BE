package com.swd392.dtos.responseDTO;

import java.time.LocalDateTime;

public record ArticleResponseDTO(

        Integer articleId,
        String title,
        String contentBody,
        String status,
        String topicName,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime approvedAt

) {}