package com.swd392.security;

import com.swd392.entities.Article;
import com.swd392.entities.User;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    // ================= TRƯỜNG HỢP TRUYỀN OBJECT =================
    @Override
    public boolean hasPermission(Authentication auth,
                                 Object targetDomainObject,
                                 Object permission) {

        if (auth == null || targetDomainObject == null) {
            return false;
        }

        if (!(targetDomainObject instanceof Article article)) {
            return false;
        }

        return checkPermission(auth, article, permission.toString());
    }

    // ================= TRƯỜNG HỢP TRUYỀN ID =================
    @Override
    public boolean hasPermission(Authentication auth,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {

        if (auth == null || targetId == null || targetType == null) {
            return false;
        }

        if (!targetType.equalsIgnoreCase("ARTICLE")) {
            return false;
        }

        Integer articleId = Integer.parseInt(targetId.toString());

        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null) return false;

        return checkPermission(auth, article, permission.toString());
    }

    // ================= LOGIC CHÍNH =================
    private boolean checkPermission(Authentication auth,
                                    Article article,
                                    String action) {

        User currentUser = userRepository
                .findByEmail(auth.getName())
                .orElse(null);

        if (currentUser == null) return false;

        User.UserRole role = currentUser.getRole();

        // ================= ADMIN =================
        if (role == User.UserRole.ADMIN) {
            return true;
        }

        // ================= LECTURE =================
        if (role == User.UserRole.LECTURE) {

            if (action.equals("VIEW")) return true;

            if (action.equals("APPROVE") || action.equals("REJECT")) return true;

            if (action.equals("UPDATE") || action.equals("DELETE")) {
                return article.getAuthor().getUserId()
                        .equals(currentUser.getUserId());
            }
        }

        // ================= STUDENT =================
        if (role == User.UserRole.STUDENT) {

            if (action.equals("VIEW")) {
                return article.getAuthor().getUserId().equals(currentUser.getUserId())
                        || article.getStatus() == Article.ArticleStatus.APPROVED;
            }

            if (action.equals("UPDATE")) {
                return article.getAuthor().getUserId().equals(currentUser.getUserId())
                        && (article.getStatus() == Article.ArticleStatus.PENDING
                        || article.getStatus() == Article.ArticleStatus.REJECTED);
            }

            if (action.equals("DELETE")) {
                return article.getAuthor().getUserId().equals(currentUser.getUserId());
            }
        }

        return false;
    }
}