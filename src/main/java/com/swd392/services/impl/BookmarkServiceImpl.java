package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.entities.Article;
import com.swd392.entities.Bookmark;
import com.swd392.entities.User;
import com.swd392.exceptions.AppException;
import com.swd392.exceptions.ResourceNotFoundException;
import com.swd392.mapper.ArticleMapper;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.BookmarkRepository;
import com.swd392.repositories.UserRepository;
import com.swd392.services.interfaces.BookmarkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;

    @Override
    public void addBookmark(Integer articleId) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n  ┌─ SERVICE ─ addBookmark\n  │ Article ID : {}", articleId);

        User user = getCurrentUser();

        if (bookmarkRepository.existsByUser_UserIdAndArticle_ArticleId(
                user.getUserId(), articleId)) {
            log.error("\n  └─ SERVICE ─ addBookmark\n    Status  : ERROR\n    Reason  : Bookmark already exists");
            throw new AppException("Bookmark already exists");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.error("\n  └─ SERVICE ─ addBookmark\n    Status  : ERROR\n    Reason  : Article not found");
                    return new ResourceNotFoundException("Article not found");
                });

        if (article.getStatus() != Article.ArticleStatus.APPROVED) {
            log.error("\n  └─ SERVICE ─ addBookmark\n    Status  : ERROR\n    Reason  : Only approved articles can be bookmarked");
            throw new AppException("Only approved articles can be bookmarked");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setArticle(article);

        bookmarkRepository.save(bookmark);
        log.info("\n  └─ SERVICE ─ addBookmark\n    Status  : SUCCESS\n    Saved Bookmark for User ID : {}", user.getUserId());
    }

    @Override
    public PaginationResponseDTO<List<ArticleResponseDTO>> getBookmarks(
            String keyword,
            int page,
            int size
    ) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n  ┌─ SERVICE ─ getBookmarks\n  │ Keyword : {}\n  │ Page    : {}\n  │ Size    : {}", keyword, page, size);

        User user = getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        Page<Bookmark> bookmarkPage;

        if (keyword == null || keyword.isBlank()) {

            bookmarkPage = bookmarkRepository
                    .findByUser_UserIdAndArticle_Status(
                            user.getUserId(),
                            Article.ArticleStatus.APPROVED,
                            pageable
                    );

        } else {

            bookmarkPage = bookmarkRepository
                    .findByUser_UserIdAndArticle_StatusAndArticle_TitleContainingIgnoreCase(
                            user.getUserId(),
                            Article.ArticleStatus.APPROVED,
                            keyword,
                            pageable
                    );
        }

        List<ArticleResponseDTO> articles = bookmarkPage.getContent()
                .stream()
                .map(Bookmark::getArticle)
                .map(articleMapper::toDTO)
                .toList();
                
        log.info("\n  └─ SERVICE ─ getBookmarks\n    Status  : SUCCESS\n    Retrieved {} bookmarks out of {}", articles.size(), bookmarkPage.getTotalElements());

        return PaginationResponseDTO.<List<ArticleResponseDTO>>builder()
                .totalItems(bookmarkPage.getTotalElements())
                .totalPages(bookmarkPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .data(articles)
                .build();
    }

    @Override
    public void deleteBookmark(Integer articleId) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n  ┌─ SERVICE ─ deleteBookmark\n  │ Article ID : {}", articleId);

        User user = getCurrentUser();

        bookmarkRepository.deleteByUser_UserIdAndArticle_ArticleId(
                user.getUserId(),
                articleId
        );
        
        log.info("\n  └─ SERVICE ─ deleteBookmark\n    Status  : SUCCESS");
    }

    @Override
    public void deleteMultiple(List<Integer> articleIds) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n  ┌─ SERVICE ─ deleteMultiple\n  │ Article IDs : {}", articleIds);

        if (articleIds == null || articleIds.isEmpty()) {
            log.error("\n  └─ SERVICE ─ deleteMultiple\n    Status  : ERROR\n    Reason  : ArticleIds cannot be empty");
            throw new AppException("ArticleIds cannot be empty");
        }

        User user = getCurrentUser();

        bookmarkRepository.deleteByUser_UserIdAndArticle_ArticleIdIn(
                user.getUserId(),
                articleIds
        );
        
        log.info("\n  └─ SERVICE ─ deleteMultiple\n    Status  : SUCCESS");
    }

    @Override
    public void deleteAll() {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("\n  ┌─ SERVICE ─ deleteAll");

        User user = getCurrentUser();

        bookmarkRepository.deleteByUser_UserId(user.getUserId());
        
        log.info("\n  └─ SERVICE ─ deleteAll\n    Status  : SUCCESS");
    }

    // ================== GET CURRENT USER ==================

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Current User retrieval failed: User not authenticated");
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Current User retrieval failed: User not found with email: " + email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }
}