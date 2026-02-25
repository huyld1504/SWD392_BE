package com.swd392.services.impl;

import com.swd392.dtos.requestDTO.ArticleRequestDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;
import com.swd392.entities.Article;
import com.swd392.entities.Topic;
import com.swd392.entities.User;
import com.swd392.exceptions.ResourceNotFoundException;
import com.swd392.mapper.ArticleMapper;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.TopicRepository;
import com.swd392.repositories.UserRepository;
import com.swd392.services.interfaces.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;

    @Override
    public ArticleResponseDTO create(ArticleRequestDTO request) {

        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Topic not found"));

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Article article = new Article();
        article.setTitle(request.title());
        article.setContentBody(request.contentBody());
        article.setTopic(topic);
        article.setAuthor(author);

        article.setStatus(Article.ArticleStatus.PENDING);

        Article saved = articleRepository.save(article);

        return articleMapper.toDTO(saved);
    }

    @Override
    public ArticleResponseDTO getById(Integer id) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Article not found"));

        return articleMapper.toDTO(article);
    }

    @Override
    public List<ArticleResponseDTO> getAll() {

        return articleRepository.findAll()
                .stream()
                .map(articleMapper::toDTO)
                .toList();
    }

    @Override
    public ArticleResponseDTO update(Integer id, ArticleRequestDTO request) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Article not found"));

        article.setTitle(request.title());
        article.setContentBody(request.contentBody());

        return articleMapper.toDTO(articleRepository.save(article));
    }

    @Override
    public void delete(Integer id) {

        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found");
        }

        articleRepository.deleteById(id);
    }

    @Override
    public ArticleResponseDTO approve(Integer articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        // 🔥 LẤY USER ĐANG ĐĂNG NHẬP
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🔥 CHECK ROLE
        if (approver.getRole() != User.UserRole.ADMIN &&
                approver.getRole() != User.UserRole.LECTURE) {
            throw new RuntimeException("You are not allowed to approve this article");
        }

        if (article.getStatus() != Article.ArticleStatus.PENDING) {
            throw new RuntimeException("Article already processed");
        }

        article.setStatus(Article.ArticleStatus.APPROVED);
        article.setApprover(approver);
        article.setApprovedAt(LocalDateTime.now());

        Article saved = articleRepository.save(article);

        return articleMapper.toDTO(saved);
    }

    @Override
    public ArticleResponseDTO reject(Integer articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();

        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ Check role giống approve
        if (approver.getRole() != User.UserRole.ADMIN &&
                approver.getRole() != User.UserRole.LECTURE) {
            throw new RuntimeException("You are not allowed to reject this article");
        }

        // ✅ Chỉ xử lý khi còn PENDING
        if (article.getStatus() != Article.ArticleStatus.PENDING) {
            throw new RuntimeException("Article already processed");
        }

        article.setStatus(Article.ArticleStatus.REJECTED);
        article.setApprover(approver);
        article.setApprovedAt(LocalDateTime.now());

        Article saved = articleRepository.save(article);

        return articleMapper.toDTO(saved);
    }
}