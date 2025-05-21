package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.*;

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

    @Test
    void getAllUsers_ShouldReturnCorrectPageOfUserResponseDto() {
        String currentUserEmail = "current@example.com";
        int page = 0, size = 2;
        String sortBy = "username", sortDir = "asc";

        Role roleUser = new Role();
        roleUser.setRoleName(RoleName.USER);
        Role roleAdmin = new Role();
        roleAdmin.setRoleName(RoleName.ADMIN);

        GoalMateUser user1 = new GoalMateUser();
        user1.setUserId(2L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setRoles(Set.of(roleUser));

        GoalMateUser user2 = new GoalMateUser();
        user2.setUserId(3L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRoles(Set.of(roleUser, roleAdmin));

        List<GoalMateUser> users = List.of(user1, user2);
        Page<GoalMateUser> userPage = new PageImpl<>(
                users, PageRequest.of(page, size, Sort.by(sortBy).ascending()), users.size());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(roleRepository.findAll()).thenReturn(List.of(roleUser, roleAdmin));

        Page<UserResponseDto> result = userService.getAllUsers(currentUserEmail, page, size, sortBy, sortDir);

        assertEquals(2, result.getContent().size());
        UserResponseDto dto1 = result.getContent().get(0);
        UserResponseDto dto2 = result.getContent().get(1);

        assertEquals(user1.getUserId(), dto1.getUserId());
        assertEquals(user1.getUsername(), dto1.getUsername());
        assertEquals(user1.getEmail(), dto1.getEmail());

        assertTrue(dto1.getRoleMap().containsKey("USER"));
        assertFalse(dto1.getRoleMap().get("USER"));

        assertTrue(dto1.getUnassignedRoles().contains("ADMIN"));
        assertTrue(dto1.getIsModifiable());

        assertTrue(dto2.getRoleMap().containsKey("ADMIN"));
        assertTrue(dto2.getRoleMap().get("ADMIN"));
        assertFalse(dto2.getRoleMap().get("USER"));

        assertTrue(dto2.getUnassignedRoles().isEmpty());
        assertTrue(dto2.getIsModifiable());
    }

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