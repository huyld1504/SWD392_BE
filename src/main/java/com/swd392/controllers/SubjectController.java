package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.requestDTO.SubjectRequestDTO;
import com.swd392.dtos.responseDTO.SubjectResponseDTO;
import com.swd392.services.interfaces.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

  private final SubjectService subjectService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<SubjectResponseDTO>> create(
      @Valid @RequestBody SubjectRequestDTO request) {

    SubjectResponseDTO result = subjectService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.<SubjectResponseDTO>builder()
            .success(true)
            .message("Subject created successfully")
            .data(result)
            .requestId(RequestContext.getRequestId())
            .timestamp(LocalDateTime.now().toString())
            .build());
  }
}
