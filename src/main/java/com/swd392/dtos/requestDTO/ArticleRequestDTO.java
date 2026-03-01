package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequestDTO {

        @NotNull(message = "TopicId is required")
        private Integer topicId;

        @NotBlank(message = "Title must not be blank")
        private String title;

        private String contentBody;
}