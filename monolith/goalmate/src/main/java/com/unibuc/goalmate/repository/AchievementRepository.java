package com.unibuc.goalmate.repository;

import com.unibuc.goalmate.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByGoal_GoalId(Long goalId);
}
