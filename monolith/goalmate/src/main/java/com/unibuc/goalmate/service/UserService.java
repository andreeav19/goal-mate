package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final GoalMateUserRepository userRepository;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(
                user -> new UserResponseDto(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream().map(
                                role -> role.getRoleName().name()
                        ).collect(Collectors.toList())
                )
        ).collect(Collectors.toList());
    }
}
