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
public class AchievementResponseDto {
    private Long achievementId;
    private String title;
    private Float amountToReach;
    private LocalDate dateAwarded;
}
