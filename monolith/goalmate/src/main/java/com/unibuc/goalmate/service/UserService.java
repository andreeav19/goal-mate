package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final GoalMateUserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(
                user -> {
                    List<String> userRoles = user.getRoles().stream()
                            .map(role -> role.getRoleName().name())
                            .sorted()
                            .toList();

                    List<String> allRoles = roleRepository.findAll().stream()
                            .map(role -> role.getRoleName().name())
                            .toList();

                    List<String> missingRoles = allRoles.stream()
                            .filter(role -> !userRoles.contains(role))
                            .toList();

                    return new UserResponseDto(
                            user.getUserId(),
                            user.getUsername(),
                            user.getEmail(),
                            userRoles,
                            missingRoles
                    );
                }
        ).toList();
    }

    public void addUserRole(UserRoleRequestDto requestDto) {
        GoalMateUser user = userRepository.findById(requestDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException("User not found.")
        );

        Role role = roleRepository.findByRoleName(RoleName.valueOf(requestDto.getRoleName())).orElseThrow(
                () -> new EntityNotFoundException("Role not found.")
        );

        if (user.getRoles().contains(role)) return;

        if (role.getUsers() == null)
            role.setUsers(new ArrayList<>());
        role.getUsers().add(user);
        user.getRoles().add(role);

        roleRepository.save(role);
        userRepository.save(user);
    }
}
