package com.swd392.mapper;

import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.dtos.responseDTO.DiagramDTO;
import com.swd392.dtos.responseDTO.UserInfoDTO;
import com.swd392.entities.Article;
import com.swd392.entities.ArticleDiagram;
import com.swd392.entities.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ArticleMapper {

        public ArticleResponseDTO toDTO(Article article) {

                List<DiagramDTO> diagrams = article.getDiagrams() != null
                                ? article.getDiagrams().stream()
                                                .map(this::toDiagramDTO)
                                                .toList()
                                : Collections.emptyList();

                UserInfoDTO authorDTO = toUserInfoDTO(article.getAuthor());
                UserInfoDTO approverDTO = article.getApprover() != null
                                ? toUserInfoDTO(article.getApprover())
                                : null;

                return new ArticleResponseDTO(
                                article.getArticleId(),
                                article.getTitle(),
                                article.getContentBody(),
                                article.getStatus().name(),
                                article.getTopic().getTopicId(),
                                article.getTopic().getName(),
                                authorDTO,
                                approverDTO,
                                diagrams,
                                article.getCreatedAt(),
                                article.getApprovedAt());
        }

        private UserInfoDTO toUserInfoDTO(User user) {
                return new UserInfoDTO(
                                user.getUserId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getAvatarUrl());
        }

        public DiagramDTO toDiagramDTO(ArticleDiagram diagram) {
                return new DiagramDTO(
                                diagram.getDiagramId(),
                                diagram.getImageUrl(),
                                diagram.getCaption(),
                                diagram.getSortOrder());
        }
}