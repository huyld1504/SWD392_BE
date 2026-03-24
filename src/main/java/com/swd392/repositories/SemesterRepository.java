package com.swd392.repositories;

import com.swd392.entities.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository
    extends JpaRepository<Semester, Integer>, JpaSpecificationExecutor<Semester> {

    Optional<Semester> findBySemesterCode(String semesterCode);

    boolean existsBySemesterCode(String semesterCode);

    /**
     * Find ACTIVE semesters where today falls within the date range.
     */
    List<Semester> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Semester.SemesterStatus status, LocalDate today1, LocalDate today2);

    List<Semester> findAllByOrderByStartDateDesc();
}
