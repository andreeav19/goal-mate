package com.unibuc.goalmate.repository;

import com.unibuc.goalmate.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findByUser_Email(String email, Pageable pageable);

}
