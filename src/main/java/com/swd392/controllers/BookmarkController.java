package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.BookmarkDeleteRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.services.interfaces.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "Bookmark management APIs")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "Add a bookmark", description = "Add a bookmark to an article for the authenticated user.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookmark added successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Article not found", content = @Content)
    })
    @PostMapping("/{articleId}")
    public ApiResponse<Void> addBookmark(
            @Parameter(description = "ID of the article to bookmark", required = true, example = "1") @PathVariable Integer articleId) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ addBookmark\n  │ Article ID : {}", articleId);

        bookmarkService.addBookmark(articleId);

        log.info("\n  └─ CONTROLLER ─ addBookmark\n    Status  : SUCCESS\n    Article ID : {}", articleId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bookmark added")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @Operation(summary = "Get bookmarks", description = "Retrieve all bookmarks for the authenticated user with pagination and optional keyword filtering.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookmarks retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @GetMapping
    public ApiResponse<PaginationResponseDTO<List<ArticleResponseDTO>>> getBookmarks(
            @Parameter(description = "Optional keyword to filter bookmarks", example = "Spring Boot") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size
    ) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ getBookmarks\n  │ Keyword : {}\n  │ Page    : {}\n  │ Size    : {}", keyword, page, size);

        PaginationResponseDTO<List<ArticleResponseDTO>> result = bookmarkService.getBookmarks(keyword, page, size);

        log.info("\n  └─ CONTROLLER ─ getBookmarks\n    Status  : SUCCESS\n    Total Items : {}\n    Total Pages : {}", result.getTotalItems(), result.getTotalPages());

        return ApiResponse.<PaginationResponseDTO<List<ArticleResponseDTO>>>builder()
                .success(true)
                .data(result)
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @Operation(summary = "Delete a bookmark", description = "Delete a bookmark for a specific article for the authenticated user.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookmark deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bookmark not found", content = @Content)
    })
    @DeleteMapping("/{articleId}")
    public ApiResponse<Void> deleteBookmark(
            @Parameter(description = "ID of the article whose bookmark is to be deleted", required = true, example = "1") @PathVariable Integer articleId) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ deleteBookmark\n  │ Article ID : {}", articleId);

        bookmarkService.deleteBookmark(articleId);

        log.info("\n  └─ CONTROLLER ─ deleteBookmark\n    Status  : SUCCESS\n    Article ID : {}", articleId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bookmark deleted")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @Operation(summary = "Delete multiple bookmarks", description = "Delete multiple bookmarks based on a list of article IDs.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookmarks deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @DeleteMapping
    public ApiResponse<Void> deleteMultiple(
            @Parameter(description = "List of article IDs to delete bookmarks for", required = true) @RequestBody BookmarkDeleteRequestDTO request) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ deleteMultiple\n  │ Article IDs : {}", request.getArticleIds());

        bookmarkService.deleteMultiple(request.getArticleIds());

        log.info("\n  └─ CONTROLLER ─ deleteMultiple\n    Status  : SUCCESS\n    Count   : {}", request.getArticleIds() != null ? request.getArticleIds().size() : 0);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bookmarks deleted")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }

    @Operation(summary = "Delete all bookmarks", description = "Delete all bookmarks for the authenticated user.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All bookmarks deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @DeleteMapping("/all")
    public ApiResponse<Void> deleteAll() {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ deleteAll");

        bookmarkService.deleteAll();

        log.info("\n  └─ CONTROLLER ─ deleteAll\n    Status  : SUCCESS");

        return ApiResponse.<Void>builder()
                .success(true)
                .message("All bookmarks deleted")
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now().toString())
                .build();
    }
}