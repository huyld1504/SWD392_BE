package com.swd392.repositories;

import com.swd392.entities.UserFeeding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFeedingRepository extends JpaRepository<UserFeeding, Integer> {

  List<UserFeeding> findByFeedingPeriodPeriodId(Integer periodId);
}
