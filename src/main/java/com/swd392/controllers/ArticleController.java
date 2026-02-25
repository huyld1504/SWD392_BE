package com.swd392.controllers;

import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.services.interfaces.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public ResponseEntity<ArticleResponseDTO> create(
            @Valid @RequestBody ArticleRequestDTO request) {

        return ResponseEntity.ok(articleService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDTO> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(articleService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponseDTO>> getAll() {

        return ResponseEntity.ok(articleService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody ArticleRequestDTO request) {

        return ResponseEntity.ok(articleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ArticleResponseDTO approve(@PathVariable Integer id) {
        return articleService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public ArticleResponseDTO reject(@PathVariable Integer id) {
        return articleService.reject(id);
    }
}