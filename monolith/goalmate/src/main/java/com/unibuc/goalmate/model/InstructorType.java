package com.unibuc.goalmate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InstructorType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructorTypeId;

    @Column(nullable = false)
    private String type;

    private String description;

    @ManyToMany(mappedBy = "instructorTypeList")
    private List<GoalMateUser> users;
}
