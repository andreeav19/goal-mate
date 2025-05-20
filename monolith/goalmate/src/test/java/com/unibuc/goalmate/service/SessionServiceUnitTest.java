package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.Session;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceUnitTest {
    private SessionService sessionService;
    private SessionRepository sessionRepository;
    private GoalRepository goalRepository;

    @BeforeEach
    void setUp() {
        goalRepository = mock(GoalRepository.class);
        sessionRepository = mock(SessionRepository.class);

        sessionService = new SessionService(sessionRepository, goalRepository);
    }

    @Test
    void addSessionToGoal_ShouldAddSessionWhenTargetNotReached() {
        Long goalId = 1L;
        Goal goal = new Goal();
        goal.setCurrentAmount(50f);
        goal.setTargetAmount(100f);
        goal.setSessions(new ArrayList<>());

        SessionRequestDto dto = new SessionRequestDto();
        dto.setDate(LocalDate.now());
        dto.setProgressAmount(30f);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        sessionService.addSessionToGoal(goalId, dto);

        assertEquals(1, goal.getSessions().size());
        assertEquals(80f, goal.getCurrentAmount());
        verify(sessionRepository).save(any(Session.class));
        verify(goalRepository).save(goal);
    }

    @Test
    void addSessionToGoal_ShouldNotAddSessionIfTargetReached() {
        Long goalId = 1L;
        Goal goal = new Goal();
        goal.setCurrentAmount(100f);
        goal.setTargetAmount(100f);
        goal.setSessions(new ArrayList<>());

        SessionRequestDto dto = new SessionRequestDto();
        dto.setDate(LocalDate.now());
        dto.setProgressAmount(10f);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        sessionService.addSessionToGoal(goalId, dto);

        assertEquals(0, goal.getSessions().size());
        assertEquals(100f, goal.getCurrentAmount());
        verify(sessionRepository, never()).save(any());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void addSessionToGoal_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        SessionRequestDto dto = new SessionRequestDto();

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> sessionService.addSessionToGoal(goalId, dto));
        verify(sessionRepository, never()).save(any());
        verify(goalRepository, never()).save(any());
    }
}