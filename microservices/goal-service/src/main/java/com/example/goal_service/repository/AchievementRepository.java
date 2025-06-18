package com.example.goal_service.repository;


import com.example.goal_service.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByGoal_GoalId(Long goalId);
}

