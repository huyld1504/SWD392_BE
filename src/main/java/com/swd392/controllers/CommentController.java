package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.CommentRequestDTO;
import com.swd392.dtos.responseDTO.CommentResponseDTO;
import com.swd392.services.interfaces.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentResponseDTO> create(
            @Valid @RequestBody CommentRequestDTO request) {

        CommentResponseDTO result = commentService.create(request);

        return ApiResponse.<CommentResponseDTO>builder()
                .success(true)
                .message("Comment created")
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<ApiResponse<PaginationResponseDTO<List<CommentResponseDTO>>>> getByArticle(
            @PathVariable Integer articleId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginationResponseDTO<List<CommentResponseDTO>> result =
                commentService.getByArticle(articleId, pageable);

        return ResponseEntity.ok(
                ApiResponse.<PaginationResponseDTO<List<CommentResponseDTO>>>builder()
                        .success(true)
                        .message("Get comments successfully")
                        .data(result)
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(@PathVariable Integer commentId) {

        commentService.delete(commentId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Comment deleted")
                .data(null)
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @PutMapping("/pin/{commentId}")
    public ApiResponse<CommentResponseDTO> pinComment(@PathVariable Integer commentId) {

        CommentResponseDTO result = commentService.pinComment(commentId);

        return ApiResponse.<CommentResponseDTO>builder()
                .success(true)
                .message("Comment pinned successfully")
                .data(result)
                .build();
    }
}