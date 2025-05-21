package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {
    @Autowired
    private GoalMateUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

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
    void getAllUsers_ShouldReturnPagedUserDtoWithCorrectAttributes() {
        GoalMateUser secondUser = new GoalMateUser();
        secondUser.setUsername("second");
        secondUser.setEmail("second@email.com");
        secondUser.setPassword("password");
        secondUser.setRoles(Set.of(adminRole, userRole));
        userRepository.save(secondUser);

        Page<UserResponseDto> result = userService.getAllUsers("test@email.com", 0, 10, "email", "asc");

        assertEquals(3, result.getTotalElements());
        List<UserResponseDto> users = result.getContent();

        for (UserResponseDto user : users) {
            assertNotNull(user.getUserId());
            assertNotNull(user.getEmail());
            assertNotNull(user.getUsername());
            assertNotNull(user.getRoleMap());
            assertNotNull(user.getUnassignedRoles());

            boolean expectedModifiable = !user.getEmail().equals("test@email.com") && user.getUserId() != 1;
            assertEquals(expectedModifiable, user.getIsModifiable());

            user.getRoleMap().forEach((roleName, canModify) -> {
                if (roleName.equals("USER")) {
                    assertFalse(canModify);
                } else {
                    assertEquals(expectedModifiable, canModify);
                }
            });

            for (String missing : user.getUnassignedRoles()) {
                assertFalse(user.getRoleMap().containsKey(missing));
            }
        }
    }

    @Test
    void getAllUsers_ShouldReturnCorrectPagination() {
        for (int i = 1; i <= 15; i++) {
            GoalMateUser user = new GoalMateUser();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@email.com");
            user.setPassword("password");
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
        }

        Page<UserResponseDto> page1 = userService.getAllUsers("test@email.com", 1, 10, "email", "asc");

        assertEquals(10, page1.getSize());
        assertEquals(1, page1.getNumber());
        assertEquals(17, page1.getTotalElements());
        assertEquals(2, page1.getTotalPages());
        assertEquals(7, page1.getContent().size());

        List<String> emails = page1.getContent().stream()
                .map(UserResponseDto::getEmail)
                .toList();
        List<String> sortedEmails = new ArrayList<>(emails);
        Collections.sort(sortedEmails);
        assertEquals(sortedEmails, emails);
    }


    @Test
    void addUserRole_ShouldAddAdminToUser() {
        UserRoleRequestDto dto = new UserRoleRequestDto(testUser.getUserId(), "ADMIN");
        userService.addUserRole(dto);

        GoalMateUser updatedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        Set<RoleName> roleNames = updatedUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        assertTrue(roleNames.contains(RoleName.ADMIN));
        assertTrue(roleNames.contains(RoleName.USER));
        assertEquals(2, roleNames.size());
    }

    @Test
    void addUserRole_ShouldNotDuplicateExistingRole() {
        UserRoleRequestDto dto = new UserRoleRequestDto(testUser.getUserId(), "USER");
        userService.addUserRole(dto);

        GoalMateUser updatedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        long userRoleCount = updatedUser.getRoles().stream()
                .filter(r -> r.getRoleName() == RoleName.USER)
                .count();

        assertEquals(1, userRoleCount);
    }

    @Test
    void addUserRole_ShouldThrowIfUserNotFound() {
        UserRoleRequestDto dto = new UserRoleRequestDto(-1L, "ADMIN");

        assertThrows(EntityNotFoundException.class, () -> userService.addUserRole(dto));
    }

    @Test
    void addUserRole_ShouldThrowIfRoleInvalid() {
        UserRoleRequestDto dto = new UserRoleRequestDto(testUser.getUserId(), "INVALID_ROLE_NAME");

        assertThrows(IllegalArgumentException.class, () -> userService.addUserRole(dto));
    }

    @Test
    void deleteUserRole_ShouldRemoveAdminRoleFromUser() {
        testUser.getRoles().add(adminRole);
        adminRole.getUsers().add(testUser);
        userRepository.save(testUser);
        roleRepository.save(adminRole);

        UserRoleRequestDto dto = new UserRoleRequestDto(testUser.getUserId(), "ADMIN");
        userService.deleteUserRole(dto);

        GoalMateUser updatedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        Set<RoleName> roleNames = updatedUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        assertFalse(roleNames.contains(RoleName.ADMIN));
        assertTrue(roleNames.contains(RoleName.USER));
        assertEquals(1, roleNames.size());
    }

    @Test
    void deleteUserRole_ShouldDoNothingIfRoleNotAssigned() {
        UserRoleRequestDto dto = new UserRoleRequestDto(testUser.getUserId(), "ADMIN");
        userService.deleteUserRole(dto);

        GoalMateUser updatedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        Set<RoleName> roleNames = updatedUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        assertTrue(roleNames.contains(RoleName.USER));
        assertFalse(roleNames.contains(RoleName.ADMIN));
        assertEquals(1, roleNames.size());
    }

    @Test
    void deleteUserRole_ShouldThrowIfUserNotFound() {
        UserRoleRequestDto dto = new UserRoleRequestDto(-1L, "ADMIN");
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUserRole(dto));
    }

    @Test
    void deleteUserRole_ShouldThrowIfRoleInvalid() {
        UserRoleRequestDto dto = new UserRoleRequestDto(testUser.getUserId(), "INVALID_ROLE_NAME");
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUserRole(dto));
    }
}