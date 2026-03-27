package com.swd392.repositories;

import com.swd392.entities.FeedingPeriod;
import com.swd392.entities.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedingPeriodRepository
        extends JpaRepository<FeedingPeriod, Integer>, JpaSpecificationExecutor<FeedingPeriod> {

    /**
     * Check if there's a feeding period with a specific status.
     */
    boolean existsByStatus(FeedingPeriod.PeriodStatus status);

    /**
     * Find feeding period by semester.
     */
    Optional<FeedingPeriod> findBySemester(Semester semester);

    /**
     * Find feeding period by semester ID.
     */
    Optional<FeedingPeriod> findBySemesterSemesterId(Integer semesterId);

    /**
     * Check if a feeding period already exists for a semester.
     */
    boolean existsBySemesterSemesterId(Integer semesterId);

    /**
     * Find all ACTIVE feeding periods.
     */
    List<FeedingPeriod> findByStatus(FeedingPeriod.PeriodStatus status);

    /**
     * Find all periods ordered by created_at descending (for admin listing).
     */
    List<FeedingPeriod> findAllByOrderByCreatedAtDesc();
}
