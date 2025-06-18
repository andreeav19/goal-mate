package com.example.goal_service.dto;

import jakarta.validation.constraints.*;
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

    @Size(max = 40, message = "Description should be 40 characters max.")
    private String description;

    @NotNull(message = "Target amount must be completed.")
    @DecimalMin(value = "1.0", message = "Target amount must be at least 1.")
    private Float targetAmount;

    @NotNull(message = "Unit for progress must be completed.")
    @NotEmpty(message = "Unit for progress must be completed.")
    private String unit;

    @FutureOrPresent(message = "Deadline cannot be in the past.")
    private LocalDate deadline;
}

