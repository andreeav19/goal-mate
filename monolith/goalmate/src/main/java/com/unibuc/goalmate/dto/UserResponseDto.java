package com.unibuc.goalmate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String username;
    private String email;
    Map<String, Boolean> roleMap;
    List<String> unassignedRoles;
    private Boolean isModifiable;
}
