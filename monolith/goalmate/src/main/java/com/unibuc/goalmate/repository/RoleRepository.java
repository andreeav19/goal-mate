package com.unibuc.goalmate.repository;

import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleName(RoleName roleName);
    Optional<Role> findByRoleName(RoleName roleName);
}
