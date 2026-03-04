package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.SubjectRequestDTO;
import com.swd392.dtos.responseDTO.SubjectResponseDTO;
import com.swd392.services.interfaces.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

  private final SubjectService subjectService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<SubjectResponseDTO>> create(
          @Valid @RequestBody SubjectRequestDTO request) {

    return buildResponse(
            subjectService.create(request),
            "Subject created successfully",
            HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PaginationResponseDTO<List<SubjectResponseDTO>>>> getAll(
          @RequestParam(value = "keyword", required = false) String keyword,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "10") int size,
          @RequestParam(value = "sort", defaultValue = "subjectId") String sort,
          @RequestParam(value = "direction", defaultValue = "desc") String direction) {

    Sort.Direction sortDirection =
            direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

    PaginationResponseDTO<List<SubjectResponseDTO>> result =
            subjectService.getAll(keyword, pageable);

    return ResponseEntity.ok(
            ApiResponse.<PaginationResponseDTO<List<SubjectResponseDTO>>>builder()
                    .success(true)
                    .message("Subjects retrieved successfully")
                    .data(result)
                    .requestId(RequestContext.getRequestId())
                    .timestamp(LocalDateTime.now().toString())
                    .build()
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<SubjectResponseDTO>> getById(
          @PathVariable Integer id) {

    return buildResponse(
            subjectService.getById(id),
            "Subject retrieved successfully",
            HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<SubjectResponseDTO>> update(
          @PathVariable Integer id,
          @Valid @RequestBody SubjectRequestDTO request) {

    return buildResponse(
            subjectService.update(id, request),
            "Subject updated successfully",
            HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(
          @PathVariable Integer id) {

    subjectService.adminDelete(id);

    return buildResponse(null,
            "Subject deleted successfully",
            HttpStatus.OK);
  }

  @PutMapping("/{id}/restore")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> restore(@PathVariable Integer id) {

    subjectService.adminRestore(id);

    return buildResponse(
            null,
            "Subject restored successfully",
            HttpStatus.OK
    );
  }

  private <T> ResponseEntity<ApiResponse<T>> buildResponse(
          T data, String message, HttpStatus status) {

    return ResponseEntity.status(status)
            .body(ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .requestId(RequestContext.getRequestId())
                    .timestamp(LocalDateTime.now().toString())
                    .build());
  }
}