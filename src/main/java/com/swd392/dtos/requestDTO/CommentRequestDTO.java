package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @NotNull
    private Integer articleId;

    private Integer parentId;

    @NotBlank
    @Size(max = 1000)
    private String content;

    @Min(1)
    @Max(5)
    private Integer ratingStar;
}