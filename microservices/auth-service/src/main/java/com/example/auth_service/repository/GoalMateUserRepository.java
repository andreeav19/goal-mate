package com.example.auth_service.repository;

import com.example.auth_service.model.GoalMateUser;
import com.example.auth_service.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalMateUserRepository extends JpaRepository<GoalMateUser, Long> {
    Optional<GoalMateUser> findByEmail(String email);
    Optional<GoalMateUser> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
