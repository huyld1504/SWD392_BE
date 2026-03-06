package com.swd392.services.impl;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.entities.Article;
import com.swd392.entities.ArticleDiagram;
import com.swd392.entities.Topic;
import com.swd392.entities.User;
import com.swd392.exceptions.AppException;
import com.swd392.exceptions.ResourceNotFoundException;
import com.swd392.mapper.ArticleMapper;
import com.swd392.repositories.ArticleDiagramRepository;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.TopicRepository;
import com.swd392.repositories.UserRepository;
import com.swd392.services.interfaces.ArticleService;
import com.swd392.services.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleDiagramRepository articleDiagramRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final ArticleMapper articleMapper;

    @Override
    public ArticleResponseDTO create(ArticleRequestDTO request, List<MultipartFile> diagrams) {

        // Get current authenticated user as author
        User author = getCurrentUser();

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContentBody(request.getContentBody());
        article.setTopic(topic);
        article.setAuthor(author);
        article.setStatus(Article.ArticleStatus.PENDING);

        Article saved = articleRepository.save(article);

        // Upload diagrams if provided
        if (diagrams != null && !diagrams.isEmpty()) {
            List<ArticleDiagram> diagramEntities = uploadDiagrams(saved, diagrams);
            saved.setDiagrams(diagramEntities);
        }

        return articleMapper.toDTO(saved);
    }

    @PreAuthorize("hasPermission(#id, 'ARTICLE', 'VIEW')")
    @Override
    public ArticleResponseDTO getById(Integer id) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        return articleMapper.toDTO(article);
    }

    @Override
    public PaginationResponseDTO<List<ArticleResponseDTO>> getAll(String keyword,Article.ArticleStatus status, Pageable pageable) {

        User currentUser = getCurrentUser();

        Specification<Article> spec = Specification.where(null);



        // ================= KEYWORD FILTER =================
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("contentBody")), "%" + keyword.toLowerCase() + "%")
            ));
        }

        // FILTER STATUS
        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        User.UserRole role = currentUser.getRole();

        // ================= STUDENT FILTER =================
        if (role == User.UserRole.STUDENT) {

            if (status == Article.ArticleStatus.APPROVED) {

                // thấy tất cả bài approved
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), Article.ArticleStatus.APPROVED)
                );

            } else {

                // chỉ thấy bài của chính mình
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("author").get("userId"), currentUser.getUserId())
                );
            }
        }

        // ADMIN & LECTURE: không cần filter thêm (xem tất cả)

        Page<Article> page = articleRepository.findAll(spec, pageable);

        List<ArticleResponseDTO> data = page.getContent()
                .stream()
                .map(articleMapper::toDTO)
                .toList();

        return PaginationResponseDTO.<List<ArticleResponseDTO>>builder()
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .data(data)
                .build();
    }

    @PreAuthorize("hasPermission(#id, 'ARTICLE', 'UPDATE')")
    @Override
    public ArticleResponseDTO update(Integer id, ArticleRequestDTO request) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        article.setTitle(request.getTitle());
        article.setContentBody(request.getContentBody());

        // nếu bài bị reject và student sửa lại -> quay lại pending
        if(article.getStatus() == Article.ArticleStatus.REJECTED){
            article.setStatus(Article.ArticleStatus.PENDING);
            article.setApprover(null);
            article.setApprovedAt(null);
        }

        return articleMapper.toDTO(articleRepository.save(article));
    }

    @Override
    @Transactional
    public void delete(Integer id) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        User currentUser = getCurrentUser();

        // ADMIN delete tất cả
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            articleRepository.delete(article);
            return;
        }

        // chỉ author mới delete
        if (!article.getAuthor().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You can only delete your own article");
        }

        // student không được delete bài approved
        if (currentUser.getRole() == User.UserRole.STUDENT &&
                article.getStatus() == Article.ArticleStatus.APPROVED) {

            throw new AppException("Student cannot delete an approved article", HttpStatus.BAD_REQUEST);
        }

        articleRepository.delete(article);
    }




    @Transactional
    public void restore(Integer id) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only admin can restore article");
        }

        articleRepository.restoreById(id);
    }

    @Override
    public ArticleResponseDTO approve(Integer articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        User approver = getCurrentUser();

        if (article.getStatus() != Article.ArticleStatus.PENDING) {
            throw new AppException("Article has already been processed", HttpStatus.BAD_REQUEST);
        }

        article.setStatus(Article.ArticleStatus.APPROVED);
        article.setApprover(approver);
        article.setApprovedAt(LocalDateTime.now());

        return articleMapper.toDTO(articleRepository.save(article));
    }

    @Override
    public ArticleResponseDTO reject(Integer articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        User approver = getCurrentUser();

        if (article.getStatus() != Article.ArticleStatus.PENDING) {
            throw new AppException("Article has already been processed", HttpStatus.BAD_REQUEST);
        }

        article.setStatus(Article.ArticleStatus.REJECTED);
        article.setApprover(approver);
        article.setApprovedAt(LocalDateTime.now());

        return articleMapper.toDTO(articleRepository.save(article));
    }

    // ===== PRIVATE HELPERS =====

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private List<ArticleDiagram> uploadDiagrams(Article article, List<MultipartFile> files) {
        List<ArticleDiagram> diagramEntities = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty())
                continue;

            try {
                String imageUrl = cloudinaryService.uploadFileImage(file);

                ArticleDiagram diagram = new ArticleDiagram();
                diagram.setArticle(article);
                diagram.setImageUrl(imageUrl);
                diagram.setSortOrder(i + 1);

                diagramEntities.add(articleDiagramRepository.save(diagram));
            } catch (IOException e) {
                log.error("Failed to upload diagram image: {}", e.getMessage());
                throw new AppException("Failed to upload diagram image: " + file.getOriginalFilename(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return diagramEntities;
    }
}