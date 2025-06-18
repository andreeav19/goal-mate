package com.example.auth_service.util;

import com.example.auth_service.model.GoalMateUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.repository.GoalMateUserRepository;
import com.example.auth_service.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final GoalMateUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
            }
        }

        if (userRepository.count() == 0) {
            GoalMateUser adminUser = new GoalMateUser();

            Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                    .orElseThrow(() -> new EntityNotFoundException("Admin role not found."));
            Role userRole = roleRepository.findByRoleName(RoleName.USER)
                    .orElseThrow(() -> new EntityNotFoundException("User role not found."));

            Hibernate.initialize(adminRole.getUsers());
            Hibernate.initialize(userRole.getUsers());

            if (adminRole.getUsers() == null) {
                adminRole.setUsers(new ArrayList<>());
            }
            if (userRole.getUsers() == null) {
                userRole.setUsers(new ArrayList<>());
            }

            adminUser.setRoles(new HashSet<>(Set.of(adminRole, userRole)));
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));

            adminRole.getUsers().add(adminUser);
            userRole.getUsers().add(adminUser);

            userRepository.save(adminUser);
        }
    }
}
