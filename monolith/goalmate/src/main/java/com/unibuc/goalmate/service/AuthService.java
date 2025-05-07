package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.LoginRequestDto;
import com.unibuc.goalmate.dto.LoginResponseDto;
import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Role;
import com.unibuc.goalmate.model.RoleName;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import com.unibuc.goalmate.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final GoalMateUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        GoalMateUser user = new GoalMateUser();
        user.setEmail(requestDto.getEmail());
        user.setUsername(requestDto.getUsername());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Role userRole = roleRepository.findByRoleName(RoleName.USER)
                        .orElseThrow(() -> new EntityNotFoundException("User role not found."));
        user.setRoles(Set.of(userRole));

        if (userRole.getUsers() == null) {
            userRole.setUsers(new ArrayList<>());
        }
        userRole.getUsers().add(user);

        userRepository.save(user);
        roleRepository.save(userRole);

        return "User registered successfully!";
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        GoalMateUser user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new RuntimeException("Invalid credentials."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());


        return new LoginResponseDto(jwtUtil.generateToken(user.getEmail(), roles), user.getUserId());
    }

}
