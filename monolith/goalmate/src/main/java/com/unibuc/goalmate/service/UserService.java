package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
