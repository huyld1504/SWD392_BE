package com.swd392.mapper;

import com.swd392.dtos.responseDTO.SubjectResponseDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;
import com.swd392.entities.Subject;
import com.swd392.entities.Topic;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SubjectMapper {

  public SubjectResponseDTO toDTO(Subject subject) {

    List<TopicResponseDTO> topics = subject.getTopics() != null
        ? subject.getTopics().stream()
            .map(this::toTopicDTO)
            .toList()
        : Collections.emptyList();

    return new SubjectResponseDTO(
        subject.getSubjectId(),
        subject.getSubjectCode(),
        subject.getName(),
        subject.getDescription(),
        topics);
  }

  public TopicResponseDTO toTopicDTO(Topic topic) {
    return new TopicResponseDTO(
        topic.getTopicId(),
        topic.getSubject().getSubjectId(),
        topic.getSubject().getName(),
        topic.getName(),
        topic.getDescription());
  }
}
