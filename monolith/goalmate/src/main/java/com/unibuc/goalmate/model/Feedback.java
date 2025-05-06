package com.unibuc.goalmate.model;

import com.unibuc.goalmate.model.key.FeedbackId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(FeedbackId.class)
public class Feedback {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private GoalMateUser instructor;

    @Id
    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(nullable = false)
    private String message;
}
