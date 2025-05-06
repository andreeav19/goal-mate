package com.unibuc.goalmate.model.key;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FeedbackId implements Serializable {
    private Long instructor;
    private Long session;
}
