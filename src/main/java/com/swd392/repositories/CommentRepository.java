package com.swd392.repositories;

import com.swd392.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Optional<Comment> findByArticleArticleIdAndIsPinnedTrue(Integer articleId);

    List<Comment> findByParentCommentId(Integer parentId);

    int countByParentCommentId(Integer parentId);



    @Transactional
    void deleteByArticleArticleId(Integer articleId);

    @Query("""
SELECT c FROM Comment c
LEFT JOIN FETCH c.user
WHERE c.article.articleId = :articleId
AND c.parent IS NULL
ORDER BY c.isPinned DESC, c.createdAt DESC
""")
    Page<Comment> findRootComments(Integer articleId, Pageable pageable);
}