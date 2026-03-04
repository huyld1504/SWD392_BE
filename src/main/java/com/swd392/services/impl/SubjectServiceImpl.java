package com.swd392.services.impl;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.SubjectRequestDTO;
import com.swd392.dtos.responseDTO.SubjectResponseDTO;
import com.swd392.entities.Article;
import com.swd392.entities.Subject;
import com.swd392.entities.Topic;
import com.swd392.exceptions.AppException;
import com.swd392.mapper.SubjectMapper;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.SubjectRepository;
import com.swd392.repositories.TopicRepository;
import com.swd392.services.interfaces.SubjectService;
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
public class SubjectServiceImpl implements SubjectService {

  private final SubjectRepository subjectRepository;
  private final SubjectMapper subjectMapper;
  private final ArticleRepository articleRepository;
  private final TopicRepository topicRepository;

  @Override
  public SubjectResponseDTO create(SubjectRequestDTO request) {

    if (subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
      throw new AppException(
              "Subject with code '" + request.getSubjectCode() + "' already exists",
              HttpStatus.CONFLICT);
    }

    Subject subject = new Subject();
    subject.setSubjectCode(request.getSubjectCode());
    subject.setName(request.getName());
    subject.setDescription(request.getDescription());

    return subjectMapper.toDTO(subjectRepository.save(subject), false);
  }

  @Override
  public PaginationResponseDTO<List<SubjectResponseDTO>> getAll(
          String keyword,
          Pageable pageable
  ) {

    Page<Subject> subjectPage = subjectRepository.findByDeletedFalse(pageable);

    List<SubjectResponseDTO> filteredList = subjectPage.getContent()
            .stream()
            .filter(subject -> keyword == null ||
                    subject.getName().toLowerCase()
                            .contains(keyword.toLowerCase()))
            .map(subject -> subjectMapper.toDTO(subject, false))
            .toList();

    return PaginationResponseDTO.<List<SubjectResponseDTO>>builder()
            .totalItems(subjectPage.getTotalElements())
            .totalPages(subjectPage.getTotalPages())
            .currentPage(subjectPage.getNumber())
            .pageSize(subjectPage.getSize())
            .data(filteredList)
            .build();
  }

  @Override
  @Transactional(readOnly = true)
  public SubjectResponseDTO getById(Integer id) {

    Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new AppException(
                    "Subject not found with id: " + id,
                    HttpStatus.NOT_FOUND));

    return subjectMapper.toDTO(subject, true); // ✅ trả topics
  }

  @Override
  public SubjectResponseDTO update(Integer id, SubjectRequestDTO request) {

    Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new AppException(
                    "Subject not found with id: " + id,
                    HttpStatus.NOT_FOUND));

    if (!subject.getSubjectCode().equals(request.getSubjectCode())
            && subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
      throw new AppException("Subject code already exists", HttpStatus.CONFLICT);
    }

    subject.setSubjectCode(request.getSubjectCode());
    subject.setName(request.getName());
    subject.setDescription(request.getDescription());

    return subjectMapper.toDTO(subjectRepository.save(subject), false);
  }

  @Transactional
  public void adminDelete(Integer id) {

    Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new AppException("Subject not found", HttpStatus.NOT_FOUND));

    // delete articles first
    for (Topic topic : subject.getTopics()) {
      for (Article article : topic.getArticles()) {
        articleRepository.delete(article); // @SQLDelete chạy
      }
      topicRepository.delete(topic); // @SQLDelete chạy
    }

    subjectRepository.delete(subject); // @SQLDelete chạy
  }

  @Transactional
  public void adminRestore(Integer id) {

    subjectRepository.restoreById(id);
    topicRepository.restoreBySubjectId(id);
    articleRepository.restoreBySubjectId(id);
  }
}