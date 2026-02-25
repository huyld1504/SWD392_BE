package com.swd392.repositories;

import com.swd392.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Integer> {
}