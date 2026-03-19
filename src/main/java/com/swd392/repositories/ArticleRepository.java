package com.swd392.repositories;

import com.swd392.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Integer>, JpaSpecificationExecutor<Article> {

    @Modifying
    @Query(value = """
    UPDATE articles 
    SET is_deleted = false 
    WHERE topic_id IN (
        SELECT topic_id FROM topics WHERE subject_id = :subjectId
    )
""", nativeQuery = true)
    int restoreBySubjectId(Integer subjectId);

    @Modifying
    @Query(value = "UPDATE articles SET is_deleted = false WHERE topic_id = :topicId", nativeQuery = true)
    int restoreByTopicId(Integer topicId);

    @Modifying
    @Query(value = "UPDATE articles SET is_deleted = false WHERE article_id = :id", nativeQuery = true)
    int restoreById(Integer id);
}