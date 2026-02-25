package com.swd392.services.interfaces;

import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;

import java.util.List;

public interface ArticleService {

    ArticleResponseDTO create(ArticleRequestDTO request);

    ArticleResponseDTO getById(Integer id);

    List<ArticleResponseDTO> getAll();

    ArticleResponseDTO update(Integer id, ArticleRequestDTO request);

    void delete(Integer id);

    ArticleResponseDTO approve(Integer articleId);

    ArticleResponseDTO reject(Integer articleId);
}