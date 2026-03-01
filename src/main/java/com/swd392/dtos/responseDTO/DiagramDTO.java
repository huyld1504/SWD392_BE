package com.swd392.dtos.responseDTO;

public record DiagramDTO(
    Integer diagramId,
    String imageUrl,
    String caption,
    Integer sortOrder) {
}
