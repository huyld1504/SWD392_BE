package com.swd392.services.interfaces;

import com.swd392.dtos.requestDTO.SubjectRequestDTO;
import com.swd392.dtos.responseDTO.SubjectResponseDTO;

public interface SubjectService {

  SubjectResponseDTO create(SubjectRequestDTO request);
}
