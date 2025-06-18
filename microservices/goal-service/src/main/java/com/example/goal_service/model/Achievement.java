package com.example.goal_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long achievementId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Float amountToReach;

    private LocalDate dateAwarded;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
}

