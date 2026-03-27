package com.swd392.repositories;

import com.swd392.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Integer>, JpaSpecificationExecutor<Article> {

    /**
     * Count approved articles by author within a date range (for semester leaderboard).
     */
    @Query("SELECT COUNT(a) FROM Article a " +
           "WHERE a.author.userId = :authorId " +
           "AND a.status = 'APPROVED' " +
           "AND a.approvedAt >= :fromDate AND a.approvedAt <= :toDate")
    long countApprovedByAuthorAndDateRange(
        @org.springframework.data.repository.query.Param("authorId") Long authorId,
        @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDateTime fromDate,
        @org.springframework.data.repository.query.Param("toDate") java.time.LocalDateTime toDate);

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