package com.unibuc.goalmate.repository;

import com.unibuc.goalmate.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUser_Email(String userEmail);
}
