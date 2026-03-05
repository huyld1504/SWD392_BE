package com.swd392.mapper;

import com.swd392.dtos.responseDTO.TopicResponseDTO;
import com.swd392.entities.Topic;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

  public TopicResponseDTO toDTO(Topic topic) {
    return new TopicResponseDTO(
            topic.getTopicId(),
            topic.getSubject().getSubjectId(),
            topic.getSubject().getName(),
            topic.getName(),
            topic.getDescription());
  }
}
