package com.example.goal_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HobbyOptionDto {
    private Long hobbyId;
    private String hobbyName;

    public HobbyOptionDto(String fallbackOption) {
    }
}
