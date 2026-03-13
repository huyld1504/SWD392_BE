package com.swd392.services.impl;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.ArticleDiagramCreateDTO;
import com.swd392.dtos.requestDTO.ArticleDiagramUpdateDTO;
import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.requestDTO.ArticleUpdateRequestDTO;
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
import com.swd392.repositories.CommentRepository;

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
    private final CommentRepository commentRepository;

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
            List<ArticleDiagram> diagramEntities = uploadDiagrams(saved, diagrams, request.getDiagramDetails());
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
    public PaginationResponseDTO<List<ArticleResponseDTO>> getAll(String keyword, Article.ArticleStatus status,
            Integer topicId, Pageable pageable) {

        User currentUser = getCurrentUser();
        Specification<Article> spec = Specification.where(null);

        // ===== KEYWORD FILTER =====
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), kw),
                    cb.like(cb.lower(root.get("contentBody")), kw)));
        }

        // ===== TOPIC FILTER =====
        if (topicId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("topic").get("topicId"), topicId));
        }

        // ===== ROLE-BASED FILTER =====
        User.UserRole role = currentUser.getRole();

        if (role == User.UserRole.STUDENT) {
            // Student chỉ thấy bài APPROVED (bất kể param status)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), Article.ArticleStatus.APPROVED));
        } else {
            // Admin & Lecture: có thể filter theo status, nếu không thì xem tất cả
            if (status != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
            }
        }

        return buildPaginationResponse(spec, pageable);
    }

    @Override
    public PaginationResponseDTO<List<ArticleResponseDTO>> getMyArticles(String keyword, Article.ArticleStatus status,
            Integer topicId, Pageable pageable) {

        User currentUser = getCurrentUser();
        Specification<Article> spec = Specification.where(null);

        // ===== CHỈ LẤY BÀI CỦA MÌNH =====
        spec = spec.and((root, query, cb) -> cb.equal(root.get("author").get("userId"), currentUser.getUserId()));

        // ===== KEYWORD FILTER =====
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), kw),
                    cb.like(cb.lower(root.get("contentBody")), kw)));
        }

        // ===== TOPIC FILTER =====
        if (topicId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("topic").get("topicId"), topicId));
        }

        // ===== STATUS FILTER =====
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return buildPaginationResponse(spec, pageable);
    }

    private PaginationResponseDTO<List<ArticleResponseDTO>> buildPaginationResponse(
            Specification<Article> spec, Pageable pageable) {

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
    public ArticleResponseDTO update(Integer id, ArticleUpdateRequestDTO request, List<MultipartFile> newDiagrams) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        // 1. Update text fields
        article.setTitle(request.getTitle());
        article.setContentBody(request.getContentBody());

        // 2. Xóa diagrams theo danh sách deleteDiagramIds (cả Cloudinary + DB)
        if (request.getDeleteDiagramIds() != null && !request.getDeleteDiagramIds().isEmpty()) {
            for (Integer diagramId : request.getDeleteDiagramIds()) {
                ArticleDiagram diagram = articleDiagramRepository.findById(diagramId)
                        .orElseThrow(() -> new ResourceNotFoundException("Diagram not found with id: " + diagramId));

                // Kiểm tra diagram thuộc article này
                if (!diagram.getArticle().getArticleId().equals(article.getArticleId())) {
                    throw new AppException("Diagram id " + diagramId + " does not belong to this article",
                            HttpStatus.BAD_REQUEST);
                }

                // Xóa trên Cloudinary
                try {
                    cloudinaryService.deleteFile(diagram.getImageUrl());
                } catch (IOException e) {
                    log.error("Failed to delete diagram from Cloudinary: {}", e.getMessage());
                }

                // Xóa trong DB
                article.getDiagrams().remove(diagram);
                articleDiagramRepository.delete(diagram);
            }
        }

        // 3. Cập nhật diagrams cũ (caption, sortOrder)
        if (request.getExistingDiagrams() != null && !request.getExistingDiagrams().isEmpty()) {
            for (ArticleDiagramUpdateDTO dto : request.getExistingDiagrams()) {
                ArticleDiagram diagram = articleDiagramRepository.findById(dto.getDiagramId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Diagram not found with id: " + dto.getDiagramId()));

                // Kiểm tra diagram thuộc article này
                if (!diagram.getArticle().getArticleId().equals(article.getArticleId())) {
                    throw new AppException("Diagram id " + dto.getDiagramId() + " does not belong to this article",
                            HttpStatus.BAD_REQUEST);
                }

                if (dto.getCaption() != null) {
                    diagram.setCaption(dto.getCaption());
                }
                if (dto.getSortOrder() != null) {
                    diagram.setSortOrder(dto.getSortOrder());
                }

                articleDiagramRepository.save(diagram);
            }
        }

        // 4. Upload diagrams mới
        if (newDiagrams != null && !newDiagrams.isEmpty()) {
            List<ArticleDiagram> newDiagramEntities = uploadDiagrams(article, newDiagrams, null);
            article.getDiagrams().addAll(newDiagramEntities);
        }

        // 5. Nếu bài bị reject và student sửa lại -> quay lại pending
        if (article.getStatus() == Article.ArticleStatus.REJECTED) {
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
            // xóa toàn bộ comment của article
            commentRepository.deleteByArticleArticleId(id);
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

        commentRepository.deleteByArticleArticleId(id);

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

    private List<ArticleDiagram> uploadDiagrams(Article article, List<MultipartFile> files,
            List<ArticleDiagramCreateDTO> diagramDetails) {
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

                // Lấy caption và sortOrder từ diagramDetails nếu có
                if (diagramDetails != null && i < diagramDetails.size()) {
                    ArticleDiagramCreateDTO detail = diagramDetails.get(i);
                    diagram.setCaption(detail.getCaption());
                    diagram.setSortOrder(detail.getSortOrder() != null ? detail.getSortOrder() : i + 1);
                } else {
                    diagram.setSortOrder(i + 1);
                }

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