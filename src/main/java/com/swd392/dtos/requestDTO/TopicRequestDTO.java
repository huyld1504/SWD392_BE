package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequestDTO {

  @NotNull(message = "SubjectId is required")
  private Integer subjectId;

  @NotBlank(message = "Name must not be blank")
  private String name;

  private String description;
}
