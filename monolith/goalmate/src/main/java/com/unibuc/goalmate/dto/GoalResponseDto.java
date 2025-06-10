package com.unibuc.goalmate.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalResponseDto {
    private Long goalId;
    private Long hobbyId;
    private String hobbyName;
    private String description;
    private Float targetAmount;
    private Float currentAmount;
    private String unit;
    private LocalDate deadline;
    private String status;
}
