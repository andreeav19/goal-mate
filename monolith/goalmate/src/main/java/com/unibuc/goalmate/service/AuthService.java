package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.LoginRequestDto;
import com.unibuc.goalmate.dto.LoginResponseDto;
import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final GoalMateUserRepository userRepository;
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
        userRepository.save(user);
        //TODO: roles

        return "User registered successfully!";
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        GoalMateUser user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new RuntimeException("Invalid credentials."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        return new LoginResponseDto(jwtUtil.generateToken(user.getEmail()), user.getUserId());
    }

}
