package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.requestDTO.TopicRequestDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;
import com.swd392.services.interfaces.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

  private final TopicService topicService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<TopicResponseDTO>> create(
      @Valid @RequestBody TopicRequestDTO request) {

    TopicResponseDTO result = topicService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.<TopicResponseDTO>builder()
            .success(true)
            .message("Topic created successfully")
            .data(result)
            .requestId(RequestContext.getRequestId())
            .timestamp(LocalDateTime.now().toString())
            .build());
  }
}
