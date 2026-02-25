package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArticleRequestDTO(

        @NotNull(message = "TopicId is required")
        Integer topicId,

        @NotNull(message = "AuthorId is required")
        Long authorId,

        @NotBlank(message = "Title must not be blank")
        String title,

        String contentBody
) {}