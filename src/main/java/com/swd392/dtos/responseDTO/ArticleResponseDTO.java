package com.swd392.dtos.responseDTO;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleResponseDTO(

                Integer articleId,
                String title,
                String contentBody,
                String status,
                Integer topicId,
                String topicName,
                UserInfoDTO author,
                UserInfoDTO approvedBy,
                List<DiagramDTO> diagrams,
                LocalDateTime createdAt,
                LocalDateTime approvedAt

) {
}