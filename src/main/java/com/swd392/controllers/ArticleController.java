package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.services.interfaces.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURE')")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> create(
            @Valid @ModelAttribute ArticleRequestDTO request,
            @RequestPart(value = "diagrams", required = false) List<MultipartFile> diagrams) {

        ArticleResponseDTO result = articleService.create(request, diagrams);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ArticleResponseDTO>builder()
                        .success(true)
                        .message("Article created successfully")
                        .data(result)
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> getById(
            @PathVariable Integer id) {

        ArticleResponseDTO result = articleService.getById(id);

        return ResponseEntity.ok(ApiResponse.<ArticleResponseDTO>builder()
                .success(true)
                .message("Article retrieved successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponseDTO<List<ArticleResponseDTO>>>> getAll(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginationResponseDTO<List<ArticleResponseDTO>> result = articleService.getAll(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.<PaginationResponseDTO<List<ArticleResponseDTO>>>builder()
                .success(true)
                .message("Articles retrieved successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ArticleRequestDTO request) {

        ArticleResponseDTO result = articleService.update(id, request);

        return ResponseEntity.ok(ApiResponse.<ArticleResponseDTO>builder()
                .success(true)
                .message("Article updated successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURE','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id) {

        articleService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Article deleted successfully")
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable Integer id) {

        articleService.restore(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Article restored successfully")
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURE')")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> approve(
            @PathVariable Integer id) {

        ArticleResponseDTO result = articleService.approve(id);

        return ResponseEntity.ok(ApiResponse.<ArticleResponseDTO>builder()
                .success(true)
                .message("Article approved successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURE')")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> reject(
            @PathVariable Integer id) {

        ArticleResponseDTO result = articleService.reject(id);

        return ResponseEntity.ok(ApiResponse.<ArticleResponseDTO>builder()
                .success(true)
                .message("Article rejected successfully")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }
}