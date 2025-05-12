package com.unibuc.goalmate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementRequestDto {
    @NotNull(message = "Title is required.")
    private String title;

    @NotNull(message = "Amount to reach must be specified.")
    private Float amountToReach;

    @NotNull(message = "Date awarded must be specified.")
    private LocalDate dateAwarded;
}
