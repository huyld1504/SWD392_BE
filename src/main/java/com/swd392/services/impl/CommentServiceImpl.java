package com.swd392.services.impl;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.CommentRequestDTO;
import com.swd392.dtos.responseDTO.CommentResponseDTO;
import com.swd392.dtos.responseDTO.UserInfoDTO;
import com.swd392.entities.Article;
import com.swd392.entities.Comment;
import com.swd392.entities.User;
import com.swd392.exceptions.AppException;
import com.swd392.exceptions.ResourceNotFoundException;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.CommentRepository;
import com.swd392.repositories.UserRepository;
import com.swd392.services.interfaces.CommentService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
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
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    private static final int MAX_REPLY = 10;

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

    @Override
    public CommentResponseDTO create(CommentRequestDTO request) {

        User user = getCurrentUser();

        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (article.getStatus() != Article.ArticleStatus.APPROVED) {
            throw new AppException("Article must be approved to comment", HttpStatus.BAD_REQUEST);
        }

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(request.getContent());

        // ===== ROOT COMMENT =====
        if (request.getParentId() == null) {

            if (request.getRatingStar() == null) {
                throw new AppException("Rating star required for root comment", HttpStatus.BAD_REQUEST);
            }

            comment.setRatingStar(request.getRatingStar());
        }

        // ===== REPLY COMMENT =====
        else {

            if (request.getRatingStar() != null) {
                throw new AppException("Reply comment cannot have rating", HttpStatus.BAD_REQUEST);
            }

            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));

            // parent phải thuộc article này
            if (!parent.getArticle().getArticleId().equals(request.getArticleId())) {
                throw new AppException("Parent comment not belong to this article", HttpStatus.BAD_REQUEST);
            }

            // chỉ cho phép 2 tầng comment
            if (parent.getParent() != null) {
                throw new AppException("Only 2-level comment allowed", HttpStatus.BAD_REQUEST);
            }

            int replyCount = commentRepository.countByParentCommentId(parent.getCommentId());

            if (replyCount >= MAX_REPLY) {
                throw new AppException("Reply limit reached", HttpStatus.BAD_REQUEST);
            }

            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);

        return mapToDTO(saved);
    }

    @Override
    public PaginationResponseDTO<List<CommentResponseDTO>> getByArticle(
            Integer articleId,
            Pageable pageable) {

        if (!articleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException("Article not found");
        }

        Page<Comment> page = commentRepository
                .findRootComments(articleId, pageable);

        List<CommentResponseDTO> comments = page.getContent()
                .stream()
                .map(this::buildTree)
                .toList();

        return PaginationResponseDTO.<List<CommentResponseDTO>>builder()
                .data(comments)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .build();
    }

    private CommentResponseDTO buildTree(Comment comment) {

        List<Comment> replyEntities =
                commentRepository.findByParentCommentId(comment.getCommentId());

        List<CommentResponseDTO> replies = replyEntities
                .stream()
                .map(this::mapToDTO)
                .toList();

        CommentResponseDTO dto = mapToDTO(comment);
        dto.setReplies(replies);

        return dto;
    }

    private CommentResponseDTO mapToDTO(Comment comment) {

        User user = comment.getUser();

        return CommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .user(
                        new UserInfoDTO(
                                user.getUserId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getAvatarUrl()
                        )
                )
                .content(comment.getContent())
                .ratingStar(comment.getRatingStar())
                .isPinned(comment.getIsPinned())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public void delete(Integer commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User user = getCurrentUser();

        // user chỉ xóa comment của mình, admin xóa được tất cả
        if (!comment.getUser().getUserId().equals(user.getUserId())
                && user.getRole() != User.UserRole.ADMIN) {

            throw new AppException("No permission to delete comment", HttpStatus.FORBIDDEN);
        }

        // nếu là ROOT COMMENT → xóa toàn bộ replies
        if (comment.getParent() == null) {

            List<Comment> replies =
                    commentRepository.findByParentCommentId(comment.getCommentId());

            if (!replies.isEmpty()) {
                commentRepository.deleteAll(replies);
            }
        }

        // xóa root hoặc reply
        commentRepository.delete(comment);
    }

    @Override
    public CommentResponseDTO pinComment(Integer commentId) {

        User currentUser = getCurrentUser();

        // chỉ admin được pin
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AppException("Only admin can pin comment", HttpStatus.FORBIDDEN);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // chỉ root comment được pin
        if (comment.getParent() != null) {
            throw new AppException("Only root comment can be pinned", HttpStatus.BAD_REQUEST);
        }

        Integer articleId = comment.getArticle().getArticleId();

        // bỏ pinned cũ nếu có
        commentRepository.findByArticleArticleIdAndIsPinnedTrue(articleId)
                .ifPresent(existingPinned -> {
                    existingPinned.setIsPinned(false);
                    commentRepository.save(existingPinned);
                });

        comment.setIsPinned(true);

        Comment saved = commentRepository.save(comment);

        return mapToDTO(saved);
    }

}