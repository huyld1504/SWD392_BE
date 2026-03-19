package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.TopicRequestDTO;
import com.swd392.dtos.responseDTO.TopicResponseDTO;
import com.swd392.services.interfaces.TopicService;
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
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

        private final TopicService topicService;

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<TopicResponseDTO>> create(
                        @Valid @RequestBody TopicRequestDTO request) {

                return buildResponse(
                                topicService.create(request),
                                "Topic created successfully",
                                HttpStatus.CREATED);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PaginationResponseDTO<List<TopicResponseDTO>>>> getAll(
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "subjectId", required = false) Integer subjectId,
                        @RequestParam(value = "subjectCode", required = false) String subjectCode,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        @RequestParam(value = "sort", defaultValue = "topicId") String sort,
                        @RequestParam(value = "direction", defaultValue = "desc") String direction) {

                Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

                PaginationResponseDTO<List<TopicResponseDTO>> result = topicService.getAll(keyword, subjectId,
                                subjectCode, pageable);

                return ResponseEntity.ok(
                                ApiResponse.<PaginationResponseDTO<List<TopicResponseDTO>>>builder()
                                                .success(true)
                                                .message("Topics retrieved successfully")
                                                .data(result)
                                                .requestId(RequestContext.getRequestId())
                                                .timestamp(LocalDateTime.now().toString())
                                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<TopicResponseDTO>> getById(
                        @PathVariable Integer id) {

                return buildResponse(
                                topicService.getById(id),
                                "Topic retrieved successfully",
                                HttpStatus.OK);
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<TopicResponseDTO>> update(
                        @PathVariable Integer id,
                        @Valid @RequestBody TopicRequestDTO request) {

                return buildResponse(
                                topicService.update(id, request),
                                "Topic updated successfully",
                                HttpStatus.OK);
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Integer id) {

                topicService.adminDelete(id);

                return buildResponse(
                                null,
                                "Topic deleted successfully",
                                HttpStatus.OK);
        }

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> restore(@PathVariable Integer id) {

                topicService.adminRestore(id);

                return buildResponse(
                                null,
                                "Topic restored successfully",
                                HttpStatus.OK);
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