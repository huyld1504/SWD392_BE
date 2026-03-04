package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.SubjectRequestDTO;
import com.swd392.dtos.responseDTO.SubjectResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubjectService {

  SubjectResponseDTO create(SubjectRequestDTO request);

  PaginationResponseDTO<List<SubjectResponseDTO>> getAll(String keyword, Pageable pageable);

  SubjectResponseDTO getById(Integer id);

  SubjectResponseDTO update(Integer id, SubjectRequestDTO request);

  void adminDelete(Integer id);

  void adminRestore(Integer id);
}