package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.BookmarkDeleteRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.services.interfaces.BookmarkService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{articleId}")
    public ApiResponse<Void> addBookmark(@PathVariable Integer articleId) {

        bookmarkService.addBookmark(articleId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bookmark added")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @GetMapping
    public ApiResponse<PaginationResponseDTO<List<ArticleResponseDTO>>> getBookmarks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ApiResponse.<PaginationResponseDTO<List<ArticleResponseDTO>>>builder()
                .success(true)
                .data(bookmarkService.getBookmarks(keyword, page, size))
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @DeleteMapping("/{articleId}")
    public ApiResponse<Void> deleteBookmark(@PathVariable Integer articleId) {

        bookmarkService.deleteBookmark(articleId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bookmark deleted")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteMultiple(@RequestBody BookmarkDeleteRequestDTO request) {

        bookmarkService.deleteMultiple(request.getArticleIds());

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bookmarks deleted")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @DeleteMapping("/all")
    public ApiResponse<Void> deleteAll() {

        bookmarkService.deleteAll();

        return ApiResponse.<Void>builder()
                .success(true)
                .message("All bookmarks deleted")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }
}