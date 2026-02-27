package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequestDTO {

  @NotBlank(message = "Subject code must not be blank")
  private String subjectCode;

  @NotBlank(message = "Name must not be blank")
  private String name;

  private String description;
}
