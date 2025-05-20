package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceUnitTest {
    private AuthService authService;
    private GoalMateUserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(GoalMateUserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authService = new AuthService(userRepository, roleRepository, passwordEncoder);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnTrueIfUserIsAdmin() {
        Authentication auth = mock(Authentication.class);
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        doReturn(authorities).when(auth).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertTrue(authService.isCurrentUserAdmin());
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnFalseIfUserIsNotAdmin() {
        Authentication auth = mock(Authentication.class);
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        doReturn(authorities).when(auth).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertFalse(authService.isCurrentUserAdmin());
    }

    @Test
    void register_ShouldThrowIfEmailExists() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new GoalMateUser()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
        assertEquals("Email is already in use.", ex.getMessage());

        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).save(any());
    }
    @Test
    void register_ShouldThrowIfUserRoleNotFound() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void register_shouldSaveUserAndRole() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("user@example.com");
        dto.setUsername("user");
        dto.setPassword("password");

        Role userRole = new Role();
        userRole.setRoleName(RoleName.USER);
        userRole.setUsers(new ArrayList<>());

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        authService.register(dto);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(dto.getEmail()) &&
                user.getUsername().equals(dto.getUsername()) &&
                user.getPassword().equals("encodedPassword") &&
                user.getRoles().contains(userRole)
        ));

        verify(roleRepository).save(userRole);
        assertTrue(
                userRole.getUsers().stream()
                        .anyMatch(u -> u.getEmail().equals(dto.getEmail())));
    }
}