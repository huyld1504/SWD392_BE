package com.swd392.dtos.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDiagramCreateDTO {

  private String caption; // Caption cho ảnh (optional)

  private Integer sortOrder; // Thứ tự hiển thị (optional, nếu null sẽ tự gán theo index)
}
