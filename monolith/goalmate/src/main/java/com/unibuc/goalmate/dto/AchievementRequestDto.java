package com.unibuc.goalmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementRequestDto {
    @NotNull(message = "Title is required.")
    private String title;

    @NotNull(message = "Amount to reach must be specified.")
    @DecimalMin(value = "0.1", message = "Progress amount must be at least 0.1.")
    private Float amountToReach;
}
