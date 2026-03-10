package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateRequestDTO {

  @NotBlank(message = "Title must not be blank")
  private String title;

  private String contentBody;

  /**
   * Danh sách diagram cũ cần cập nhật (caption, sortOrder).
   * Chỉ gửi những diagram muốn thay đổi.
   */
  private List<ArticleDiagramUpdateDTO> existingDiagrams;

  /**
   * Danh sách diagramId cần xóa (sẽ xóa cả trên Cloudinary).
   */
  private List<Integer> deleteDiagramIds;
}
