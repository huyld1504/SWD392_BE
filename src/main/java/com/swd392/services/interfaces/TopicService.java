package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.TopicRequestDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopicService {

  TopicResponseDTO create(TopicRequestDTO request);

  PaginationResponseDTO<List<TopicResponseDTO>> getAll(
          String keyword,
          Integer subjectId,
          Pageable pageable
  );

  TopicResponseDTO getById(Integer id);

  TopicResponseDTO update(Integer id, TopicRequestDTO request);

  void adminDelete(Integer id);

  void adminRestore(Integer id);
}