package com.example.hobby_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HobbyOptionResponseDto {
    private Long hobbyId;
    private String hobbyName;
}
