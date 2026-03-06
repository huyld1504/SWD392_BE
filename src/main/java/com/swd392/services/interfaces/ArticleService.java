package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.entities.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {

    ArticleResponseDTO create(ArticleRequestDTO request, List<MultipartFile> diagrams);

    ArticleResponseDTO getById(Integer id);

    PaginationResponseDTO<List<ArticleResponseDTO>> getAll(String keyword, Article.ArticleStatus status, Pageable pageable);

    ArticleResponseDTO update(Integer id, ArticleRequestDTO request);

    void delete(Integer id);

    void restore(Integer id);

    ArticleResponseDTO approve(Integer articleId);

    ArticleResponseDTO reject(Integer articleId);
}