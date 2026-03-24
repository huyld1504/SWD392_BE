package com.swd392.repositories;

import com.swd392.entities.UserFeeding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFeedingRepository extends JpaRepository<UserFeeding, Integer> {

    List<UserFeeding> findByFeedingPeriodPeriodId(Integer periodId);

    /**
     * Check if a user has already been fed in a specific period.
     */
    boolean existsByFeedingPeriodPeriodIdAndUserUserId(Integer periodId, Long userId);

    /**
     * Get all user IDs that have been fed in a specific period.
     * Used by the scheduler to find unfed users.
     */
    @Query("SELECT uf.user.userId FROM UserFeeding uf WHERE uf.feedingPeriod.periodId = :periodId")
    List<Long> findFedUserIdsByPeriodId(@Param("periodId") Integer periodId);

    /**
     * Count users fed in a specific period.
     */
    int countByFeedingPeriodPeriodId(Integer periodId);
}
