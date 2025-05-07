package com.unibuc.goalmate.repository;

import com.unibuc.goalmate.model.GoalMateUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalMateUserRepository extends JpaRepository<GoalMateUser, Long> {
    Optional<GoalMateUser> findByEmail(String email);
}
