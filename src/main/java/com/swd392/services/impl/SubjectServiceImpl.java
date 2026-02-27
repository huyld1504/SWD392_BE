package com.swd392.services.impl;

import com.swd392.dtos.requestDTO.SubjectRequestDTO;
import com.swd392.dtos.responseDTO.SubjectResponseDTO;
import com.swd392.entities.Subject;
import com.swd392.exceptions.AppException;
import com.swd392.mapper.SubjectMapper;
import com.swd392.repositories.SubjectRepository;
import com.swd392.services.interfaces.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubjectServiceImpl implements SubjectService {

  private final SubjectRepository subjectRepository;
  private final SubjectMapper subjectMapper;

  @Override
  public SubjectResponseDTO create(SubjectRequestDTO request) {
    log.info("Creating subject with code: {}", request.getSubjectCode());

    if (subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
      throw new AppException("Subject with code '" + request.getSubjectCode() + "' already exists",
          HttpStatus.CONFLICT);
    }

    Subject subject = new Subject();
    subject.setSubjectCode(request.getSubjectCode());
    subject.setName(request.getName());
    subject.setDescription(request.getDescription());

    Subject saved = subjectRepository.save(subject);
    log.info("Subject created successfully with id: {}", saved.getSubjectId());

    return subjectMapper.toDTO(saved);
  }
}
