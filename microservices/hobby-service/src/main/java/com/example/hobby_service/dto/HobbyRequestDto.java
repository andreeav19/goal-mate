package com.example.hobby_service.dto;

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
public class HobbyRequestDto {
    @NotNull(message = "Name must be specified.")
    @NotEmpty(message = "Name must be specified.")
    private String name;

    private String description;
}
