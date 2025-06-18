package com.example.goal_service.model;

import jakarta.persistence.*;
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
@Entity
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    private String description;

    @Column(nullable = false)
    private Float targetAmount;

    private Float currentAmount;

    @Column(nullable = false)
    private String targetUnit;

    private LocalDate deadline;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long hobbyId;


    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<Session> sessions;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<Achievement> achievements;

    @PrePersist
    private void ensureCurrentAmountDefault() {
        if (currentAmount == null) {
            currentAmount = 0.0f;
        }
    }
}

