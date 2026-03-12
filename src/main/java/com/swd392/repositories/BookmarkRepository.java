package com.swd392.repositories;

import com.swd392.entities.Article;
import com.swd392.entities.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Bookmark.BookmarkId> {

    boolean existsByUser_UserIdAndArticle_ArticleId(Long userId, Integer articleId);

    @EntityGraph(attributePaths = {"article"})
    Page<Bookmark> findByUser_UserIdAndArticle_Status(
            Long userId,
            Article.ArticleStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"article"})
    Page<Bookmark> findByUser_UserIdAndArticle_StatusAndArticle_TitleContainingIgnoreCase(
            Long userId,
            Article.ArticleStatus status,
            String keyword,
            Pageable pageable
    );

    void deleteByUser_UserIdAndArticle_ArticleId(Long userId, Integer articleId);

    void deleteByUser_UserIdAndArticle_ArticleIdIn(Long userId, List<Integer> articleIds);

    void deleteByUser_UserId(Long userId);
}