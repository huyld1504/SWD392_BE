package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequestDTO {

        @NotNull(message = "TopicId is required")
        private Integer topicId;

        @NotBlank(message = "Title must not be blank")
        private String title;

        private String contentBody;

        /**
         * Metadata cho từng diagram (caption, sortOrder).
         * Index map 1:1 với file trong List<MultipartFile> diagrams.
         * Ví dụ: diagramDetails[0] tương ứng với diagrams[0].
         * Optional — nếu không gửi, caption = null và sortOrder tự gán theo index.
         */
        private List<ArticleDiagramCreateDTO> diagramDetails;
}
