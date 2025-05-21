package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class AuthServiceIntegrationTest {
    @Autowired
    private GoalMateUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private GoalMateUser testUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setup() {
        userRole = roleRepository.findByRoleName(RoleName.USER).orElseThrow();
        adminRole = roleRepository.findByRoleName(RoleName.ADMIN).orElseThrow();

        testUser = new GoalMateUser();
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setPassword("test");
        testUser.setRoles(new HashSet<>(Set.of(userRole)));

        roleRepository.save(userRole);
        userRepository.save(testUser);
    }

    @Test
    void register_ShouldSaveNewUserWithEncodedPasswordAndUserRole() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("user@email.com");
        request.setUsername("user");
        request.setPassword("user");

        authService.register(request);

        Optional<GoalMateUser> savedUserOpt = userRepository.findByEmail("user@email.com");
        assertTrue(savedUserOpt.isPresent());

        GoalMateUser savedUser = savedUserOpt.get();
        assertEquals("user", savedUser.getUsername());
        assertNotEquals("user", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("user", savedUser.getPassword()));
        assertTrue(savedUser.getRoles().contains(userRole));

        Role role = roleRepository.findByRoleName(RoleName.USER).orElseThrow();
        assertTrue(role.getUsers().contains(savedUser));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        GoalMateUser existingUser = new GoalMateUser();
        existingUser.setEmail("user@email.com");
        existingUser.setUsername("user");
        existingUser.setPassword(passwordEncoder.encode("user"));
        existingUser.setRoles(Set.of(userRole));
        userRepository.save(existingUser);

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("user@email.com");
        request.setUsername("newuser");
        request.setPassword("newuser");

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnTrue_WhenUserHasAdminRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "adminUser",
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(authService.isCurrentUserAdmin());
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnFalse_WhenUserDoesNotHaveAdminRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "regularUser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(authService.isCurrentUserAdmin());
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnFalse_WhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertFalse(authService.isCurrentUserAdmin());
    }
}