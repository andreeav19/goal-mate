package com.unibuc.goalmate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalSessionsResponseDto {
    private Long goalId;
    private String goalName;
    private String unit;
    private List<SessionResponseDto> sessions;
}
