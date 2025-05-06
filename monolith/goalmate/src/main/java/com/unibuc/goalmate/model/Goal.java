package com.unibuc.goalmate.model;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private GoalMateUser user;

    @ManyToOne
    @JoinColumn(name = "hobby_id", nullable = false)
    private Hobby hobby;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<Session> sessions;

    @PrePersist
    private void ensureCurrentAmountDefault() {
        if (currentAmount == null) {
            currentAmount = 0.0f;
        }
    }
}
