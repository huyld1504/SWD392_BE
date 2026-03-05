package com.swd392.repositories;

import com.swd392.entities.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

  boolean existsBySubjectCode(String subjectCode);

  Page<Subject> findByDeletedFalse(Pageable pageable);

  @Modifying
  @Query(value = "UPDATE subjects SET is_deleted = false WHERE subject_id = :id", nativeQuery = true)
  int restoreById(Integer id);

  @Modifying
  @Query("""
UPDATE Subject s
SET s.deleted = true
WHERE s.subjectId = :id
""")
  int softDeleteById(Integer id);
}
