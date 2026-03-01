package com.swd392.services.interfaces;

import com.swd392.dtos.requestDTO.TopicRequestDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;

public interface TopicService {

  TopicResponseDTO create(TopicRequestDTO request);
}
