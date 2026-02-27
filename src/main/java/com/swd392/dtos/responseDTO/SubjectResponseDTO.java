package com.swd392.dtos.responseDTO;

import java.util.List;

public record SubjectResponseDTO(
    Integer subjectId,
    String subjectCode,
    String name,
    String description,
    List<TopicResponseDTO> topics) {
}
