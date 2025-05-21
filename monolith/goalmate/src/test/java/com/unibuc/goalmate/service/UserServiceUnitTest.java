package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {
    private UserService userService;
    private GoalMateUserRepository userRepository;
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(GoalMateUserRepository.class);
        roleRepository = mock(RoleRepository.class);

        userService = new UserService(userRepository, roleRepository);
    }

    //TODO: getAllUsers

    @Test
    void addUserRole_ShouldThrowIfUserNotFound() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.addUserRole(dto));
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUserRole_ShouldThrowIfRoleInvalid() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("INVALID");

        GoalMateUser user = new GoalMateUser();
        user.setRoles(new HashSet<>());

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.addUserRole(dto));
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUserRole_ShouldThrowIfRoleNotFound() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("USER");

        GoalMateUser user = new GoalMateUser();
        user.setRoles(new HashSet<>());

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.addUserRole(dto));
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void addUserRole_ShouldAddRoleIfNotPresent() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("USER");

        GoalMateUser user = new GoalMateUser();
        user.setRoles(new HashSet<>());

        Role role = new Role();
        role.setRoleName(RoleName.USER);
        role.setUsers(new ArrayList<>());

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(role));

        userService.addUserRole(dto);

        assertTrue(user.getRoles().contains(role));
        assertTrue(role.getUsers().contains(user));

        verify(roleRepository).save(role);
        verify(userRepository).save(user);
    }

    @Test
    void addUserRole_ShouldReturnIfUserAlreadyHasRole() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("USER");

        Role role = new Role();
        role.setRoleName(RoleName.USER);

        GoalMateUser user = new GoalMateUser();
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(role));

        userService.addUserRole(dto);

        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserRole_ShouldDeleteRoleFromUserAndSave() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("USER");

        GoalMateUser user = new GoalMateUser();
        Role role = new Role();
        role.setRoleName(RoleName.USER);
        role.setUsers(new ArrayList<>());
        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        role.getUsers().add(user);

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(role));

        userService.deleteUserRole(dto);

        assertFalse(user.getRoles().contains(role));
        assertFalse(role.getUsers().contains(user));

        verify(roleRepository).save(role);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserRole_ShouldReturnIfUserDoesNotHaveRole() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("USER");

        GoalMateUser user = new GoalMateUser();
        user.setRoles(new HashSet<>());

        Role role = new Role();
        role.setRoleName(RoleName.USER);
        role.setUsers(new ArrayList<>());

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(role));

        userService.deleteUserRole(dto);

        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserRole_ShouldThrowIfUserNotFound() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);

        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUserRole(dto));
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserRole_ShouldThrowIfRoleNotFound() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("USER");

        GoalMateUser user = new GoalMateUser();
        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUserRole(dto));
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserRole_ShouldThrowIfRoleInvalid() {
        UserRoleRequestDto dto = new UserRoleRequestDto();
        dto.setUserId(1L);
        dto.setRoleName("INVALID");

        GoalMateUser user = new GoalMateUser();
        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUserRole(dto));
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}