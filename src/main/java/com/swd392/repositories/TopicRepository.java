package com.swd392.repositories;

import com.swd392.entities.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TopicRepository extends JpaRepository<Topic, Integer>, JpaSpecificationExecutor<Topic> {

    Page<Topic> findBySubjectSubjectId(Integer subjectId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE topics SET is_deleted = false WHERE subject_id = :subjectId", nativeQuery = true)
    int restoreBySubjectId(Integer subjectId);

    @Modifying
    @Query(value = "UPDATE topics SET is_deleted = false WHERE topic_id = :id", nativeQuery = true)
    int restoreById(Integer id);

}