package com.swd392.services.impl;

import com.swd392.dtos.requestDTO.TopicRequestDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;
import com.swd392.entities.Subject;
import com.swd392.entities.Topic;
import com.swd392.exceptions.ResourceNotFoundException;
import com.swd392.mapper.TopicMapper;
import com.swd392.repositories.SubjectRepository;
import com.swd392.repositories.TopicRepository;
import com.swd392.services.interfaces.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TopicServiceImpl implements TopicService {

  private final TopicRepository topicRepository;
  private final SubjectRepository subjectRepository;
  private final TopicMapper topicMapper;

  @Override
  public TopicResponseDTO create(TopicRequestDTO request) {
    log.info("Creating topic with name: {} for subjectId: {}", request.getName(), request.getSubjectId());

    Subject subject = subjectRepository.findById(request.getSubjectId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Subject not found with id: " + request.getSubjectId()));

    Topic topic = new Topic();
    topic.setSubject(subject);
    topic.setName(request.getName());
    topic.setDescription(request.getDescription());

    Topic saved = topicRepository.save(topic);
    log.info("Topic created successfully with id: {}", saved.getTopicId());

    return topicMapper.toDTO(saved);
  }
}
