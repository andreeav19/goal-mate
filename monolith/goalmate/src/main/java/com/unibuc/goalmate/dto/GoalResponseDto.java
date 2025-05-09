package com.unibuc.goalmate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponseDto {
    private String hobbyName;
    private String description;
    private Float targetAmount;
    private Float currentAmount;
    private String unit;
    private LocalDate deadline;
}
