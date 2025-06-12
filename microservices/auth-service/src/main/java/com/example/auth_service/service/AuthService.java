package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponseDto;
import com.example.auth_service.dto.LoginRequestDto;
import com.example.auth_service.dto.RegisterRequestDto;
import com.example.auth_service.model.GoalMateUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.repository.GoalMateUserRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.security.CustomUserDetailsService;
import com.example.auth_service.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoalMateUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public void register(RegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        GoalMateUser user = new GoalMateUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role role = roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    public AuthResponseDto authenticate(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
        String jwt = jwtUtil.generateToken(userDetails);
        return new AuthResponseDto(jwt);
    }
}

