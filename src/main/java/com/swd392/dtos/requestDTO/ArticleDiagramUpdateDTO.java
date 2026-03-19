package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDiagramUpdateDTO {

  @NotNull(message = "Diagram ID must not be null")
  private Integer diagramId; // ID của diagram cần cập nhật (bắt buộc)

  private String caption; // Caption mới (null = không thay đổi)

  private Integer sortOrder; // Thứ tự mới (null = không thay đổi)
}
