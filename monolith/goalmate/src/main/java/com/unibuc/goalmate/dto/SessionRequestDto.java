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
public class SessionRequestDto {
    @NotNull(message = "Session date must be completed.")
    private LocalDate date;

    @NotNull(message = "Session progress must be specified.")
    private Float progressAmount;
}
