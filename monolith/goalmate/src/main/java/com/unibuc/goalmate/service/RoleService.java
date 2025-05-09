package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.RoleResponseDto;
import com.unibuc.goalmate.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAll().stream().map(
                role -> new RoleResponseDto(
                        role.getRoleId(),
                        role.getRoleName().name()
                )
        ).collect(Collectors.toList());
    }
}
