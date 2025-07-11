package com.example.goal_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalSessionsResponseDto {
    private Long goalId;
    private String goalName;
    private String unit;
    private Float targetAmount;
    private Float currentAmount;
    private LocalDate goalDeadline;
    private List<SessionResponseDto> sessions;
    private boolean hasNext;
}

