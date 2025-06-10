package com.unibuc.goalmate.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequestDto {
    @NotNull(message = "User id must not be null.")
    private Long userId;

    @NotNull(message = "Role name must not be null.")
    @NotEmpty(message = "Role name must be completed.")
    private String roleName;
}
