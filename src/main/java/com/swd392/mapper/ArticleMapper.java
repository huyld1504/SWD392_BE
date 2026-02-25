package com.swd392.mapper;

import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.entities.Article;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {

    public ArticleResponseDTO toDTO(Article article) {

        return new ArticleResponseDTO(
                article.getArticleId(),
                article.getTitle(),
                article.getContentBody(),
                article.getStatus().name(),
                article.getTopic().getName(),
                article.getAuthor().getFullName(),
                article.getCreatedAt(),
                article.getApprovedAt()
        );
    }
}