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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final GoalMateUserRepository userRepository;
    private final RoleRepository roleRepository;

    public Page<UserResponseDto> getAllUsers(String userEmail, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<GoalMateUser> userPage = userRepository.findAll(pageable);

        List<UserResponseDto> userDtos = userPage.stream().map(user -> {
            Map<String, Boolean> roleMap = user.getRoles().stream()
                    .map(role -> role.getRoleName().name())
                    .sorted()
                    .collect(Collectors.toMap(
                            roleName -> roleName,
                            roleName -> !roleName.equals("USER"),
                            (existing, replacement) -> replacement,
                            TreeMap::new
                    ));

            List<String> allRoles = roleRepository.findAll().stream()
                    .map(role -> role.getRoleName().name())
                    .toList();

            List<String> missingRoles = allRoles.stream()
                    .filter(role -> !roleMap.containsKey(role))
                    .toList();

            boolean isModifiable = !Objects.equals(userEmail, user.getEmail()) && user.getUserId() != 1;
            if (!isModifiable) {
                roleMap.replaceAll((k, v) -> false);
            }

            return new UserResponseDto(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    roleMap,
                    missingRoles,
                    isModifiable
            );
        }).toList();

        return new PageImpl<>(userDtos, pageable, userPage.getTotalElements());
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

    public void deleteUserRole(UserRoleRequestDto requestDto) {
        GoalMateUser user = userRepository.findById(requestDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException("User not found.")
        );

        Role role = roleRepository.findByRoleName(RoleName.valueOf(requestDto.getRoleName())).orElseThrow(
                () -> new EntityNotFoundException("Role not found.")
        );

        if (!user.getRoles().contains(role)) return;

        role.getUsers().remove(user);
        user.getRoles().remove(role);

        roleRepository.save(role);
        userRepository.save(user);
    }
}
