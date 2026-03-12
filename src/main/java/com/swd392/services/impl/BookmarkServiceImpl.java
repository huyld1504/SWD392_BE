package com.swd392.services.impl;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        User user = getCurrentUser();

        if (bookmarkRepository.existsByUser_UserIdAndArticle_ArticleId(
                user.getUserId(), articleId)) {

            throw new AppException("Bookmark already exists");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (article.getStatus() != Article.ArticleStatus.APPROVED) {
            throw new AppException("Only approved articles can be bookmarked");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setArticle(article);

        bookmarkRepository.save(bookmark);
    }

    @Override
    public PaginationResponseDTO<List<ArticleResponseDTO>> getBookmarks(
            String keyword,
            int page,
            int size
    ) {

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

        User user = getCurrentUser();

        bookmarkRepository.deleteByUser_UserIdAndArticle_ArticleId(
                user.getUserId(),
                articleId
        );
    }

    @Override
    public void deleteMultiple(List<Integer> articleIds) {

        if (articleIds == null || articleIds.isEmpty()) {
            throw new AppException("ArticleIds cannot be empty");
        }

        User user = getCurrentUser();

        bookmarkRepository.deleteByUser_UserIdAndArticle_ArticleIdIn(
                user.getUserId(),
                articleIds
        );
    }

    @Override
    public void deleteAll() {

        User user = getCurrentUser();

        bookmarkRepository.deleteByUser_UserId(user.getUserId());
    }

    // ================== GET CURRENT USER ==================

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));
    }
}