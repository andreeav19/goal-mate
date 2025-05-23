package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.Session;
import com.unibuc.goalmate.repository.AchievementRepository;
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
    private AchievementService achievementService;

    @BeforeEach
    void setUp() {
        goalRepository = mock(GoalRepository.class);
        sessionRepository = mock(SessionRepository.class);
        achievementService = mock(AchievementService.class);

        sessionService = new SessionService(sessionRepository, goalRepository, achievementService);
    }

    @Test
    void addSessionToGoal_ShouldAddSessionWhenTargetNotReached() {
        Long goalId = 1L;
        Goal goal = new Goal();
        goal.setCurrentAmount(50f);
        goal.setTargetAmount(100f);
        goal.setSessions(new ArrayList<>());

        SessionRequestDto dto = new SessionRequestDto();
        dto.setProgressAmount(30f);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        sessionService.addSessionToGoal(goalId, dto);

        assertEquals(1, goal.getSessions().size());
        assertEquals(80f, goal.getCurrentAmount());
        verify(sessionRepository).save(any(Session.class));
        verify(goalRepository).save(goal);
        verify(achievementService).checkAchievements(goalId, dto);
    }

    @Test
    void addSessionToGoal_ShouldNotAddSessionIfTargetReached() {
        Long goalId = 1L;
        Goal goal = new Goal();
        goal.setCurrentAmount(100f);
        goal.setTargetAmount(100f);
        goal.setSessions(new ArrayList<>());

        SessionRequestDto dto = new SessionRequestDto();
        dto.setProgressAmount(10f);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        sessionService.addSessionToGoal(goalId, dto);

        assertEquals(0, goal.getSessions().size());
        assertEquals(100f, goal.getCurrentAmount());
        verify(sessionRepository, never()).save(any());
        verify(goalRepository, never()).save(any());
        verify(achievementService, never()).checkAchievements(any(), any());
    }

    @Test
    void addSessionToGoal_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        SessionRequestDto dto = new SessionRequestDto();

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> sessionService.addSessionToGoal(goalId, dto));
        verify(sessionRepository, never()).save(any());
        verify(goalRepository, never()).save(any());
        verify(achievementService, never()).checkAchievements(any(), any());
    }

    @Test
    void deleteSessionFromGoal_ShouldDeleteSessionAndUpdateGoal() {
        Long goalId = 1L;
        Long sessionId = 2L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setCurrentAmount(50f);
        goal.setDeadline(LocalDate.of(2025, 12, 31));
        goal.setSessions(new ArrayList<>());

        Session session = new Session();
        session.setSessionId(sessionId);
        session.setGoal(goal);
        session.setProgressAmount(20f);
        session.setDate(LocalDate.of(2025, 12, 1));

        goal.getSessions().add(session);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionService.deleteSessionFromGoal(goalId, sessionId);

        assertEquals(30f, goal.getCurrentAmount());
        assertFalse(goal.getSessions().contains(session));
        verify(goalRepository).save(goal);
        verify(sessionRepository).delete(session);
    }

    @Test
    void deleteSessionFromGoal_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        Long sessionId = 2L;

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> sessionService.deleteSessionFromGoal(goalId, sessionId));
        verify(sessionRepository, never()).delete(any());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void deleteSessionFromGoal_ShouldThrowIfSessionNotFound() {
        Long goalId = 1L;
        Long sessionId = 2L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> sessionService.deleteSessionFromGoal(goalId, sessionId));
        verify(sessionRepository, never()).delete(any());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void deleteSessionFromGoal_ShouldThrowIfSessionDoesNotBelongToGoal() {
        Long goalId = 1L;
        Long sessionId = 2L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);

        Goal anotherGoal = new Goal();
        anotherGoal.setGoalId(99L);

        Session session = new Session();
        session.setSessionId(sessionId);
        session.setGoal(anotherGoal);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () -> sessionService.deleteSessionFromGoal(goalId, sessionId));
        verify(sessionRepository, never()).delete(any());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void deleteSessionFromGoal_ShouldThrowIfSessionDateAfterGoalDeadline() {
        Long goalId = 1L;
        Long sessionId = 2L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setDeadline(LocalDate.of(2025, 12, 31));

        Session session = new Session();
        session.setSessionId(sessionId);
        session.setGoal(goal);
        session.setDate(LocalDate.of(2026, 1, 1));

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(RuntimeException.class, () -> sessionService.deleteSessionFromGoal(goalId, sessionId));
        verify(sessionRepository, never()).delete(any());
        verify(goalRepository, never()).save(any());
    }
}