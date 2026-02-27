package com.swd392.dtos.responseDTO;

public record TopicResponseDTO(
    Integer topicId,
    Integer subjectId,
    String subjectName,
    String name,
    String description) {
}
