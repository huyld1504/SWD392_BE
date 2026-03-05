package com.swd392.services.impl;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.TopicRequestDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;
import com.swd392.entities.Article;
import com.swd392.entities.Subject;
import com.swd392.entities.Topic;
import com.swd392.exceptions.AppException;
import com.swd392.mapper.TopicMapper;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.SubjectRepository;
import com.swd392.repositories.TopicRepository;
import com.swd392.services.interfaces.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TopicServiceImpl implements TopicService {

  private final TopicRepository topicRepository;
  private final SubjectRepository subjectRepository;
  private final TopicMapper topicMapper;
  private final ArticleRepository articleRepository;

  @Override
  public TopicResponseDTO create(TopicRequestDTO request) {

    Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new AppException(
                    "Subject not found with id: " + request.getSubjectId(),
                    HttpStatus.NOT_FOUND));

    Topic topic = new Topic();
    topic.setSubject(subject);
    topic.setName(request.getName());
    topic.setDescription(request.getDescription());

    return topicMapper.toDTO(topicRepository.save(topic));
  }

  @Override
  public PaginationResponseDTO<List<TopicResponseDTO>> getAll(
          String keyword,
          Integer subjectId,
          Pageable pageable) {

    Page<Topic> topicPage;

    if (subjectId != null) {
      topicPage = topicRepository.findBySubjectSubjectId(subjectId, pageable);
    } else {
      topicPage = topicRepository.findAll(pageable);
    }

    List<TopicResponseDTO> filteredList = topicPage.getContent()
            .stream()
            .filter(topic ->
                    keyword == null ||
                            topic.getName().toLowerCase()
                                    .contains(keyword.toLowerCase()))
            .map(topicMapper::toDTO)
            .toList();

    return PaginationResponseDTO.<List<TopicResponseDTO>>builder()
            .totalItems(topicPage.getTotalElements())
            .totalPages(topicPage.getTotalPages())
            .currentPage(topicPage.getNumber())
            .pageSize(topicPage.getSize())
            .data(filteredList)
            .build();
  }

  @Override
  @Transactional(readOnly = true)
  public TopicResponseDTO getById(Integer id) {

    Topic topic = topicRepository.findById(id)
            .orElseThrow(() -> new AppException(
                    "Topic not found with id: " + id,
                    HttpStatus.NOT_FOUND));

    return topicMapper.toDTO(topic);
  }

  @Override
  public TopicResponseDTO update(Integer id, TopicRequestDTO request) {

    Topic topic = topicRepository.findById(id)
            .orElseThrow(() -> new AppException(
                    "Topic not found with id: " + id,
                    HttpStatus.NOT_FOUND));

    Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new AppException(
                    "Subject not found with id: " + request.getSubjectId(),
                    HttpStatus.NOT_FOUND));

    topic.setSubject(subject);
    topic.setName(request.getName());
    topic.setDescription(request.getDescription());

    return topicMapper.toDTO(topicRepository.save(topic));
  }

  @Transactional
  public void adminDelete(Integer id) {

    Topic topic = topicRepository.findById(id)
            .orElseThrow(() -> new AppException("Topic not found", HttpStatus.NOT_FOUND));

    for (Article article : topic.getArticles()) {
      articleRepository.delete(article);
    }

    topicRepository.delete(topic);
  }

  @Transactional
  public void adminRestore(Integer id) {

    topicRepository.restoreById(id);
    articleRepository.restoreByTopicId(id);
  }


}