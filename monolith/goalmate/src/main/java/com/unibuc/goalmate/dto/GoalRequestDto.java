package com.unibuc.goalmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
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
public class GoalRequestDto {
    @NotNull(message = "Hobby should be specified.")
    private Long hobbyId;

    private String description;

    @NotNull(message = "Target amount must be completed.")
    @DecimalMin(value = "1.0", message = "Target amount must be at least 1.")
    private Float targetAmount;

    @NotNull(message = "Unit for progress must be completed.")
    private String unit;

    @FutureOrPresent(message = "Deadline cannot be in the past.")
    private LocalDate deadline;
}
