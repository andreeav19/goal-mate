package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.AchievementRequestDto;
import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Achievement;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.repository.AchievementRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AchievementServiceUnitTest {
    private AchievementRepository achievementRepository;
    private GoalRepository goalRepository;
    private AchievementService achievementService;

    @BeforeEach
    void setUp() {
        achievementRepository = mock(AchievementRepository.class);
        goalRepository = mock(GoalRepository.class);

        achievementService = new AchievementService(achievementRepository, goalRepository);
    }

    @Test
    void addAchievementToGoal_ShouldAddAchievement_WhenGoalExists() {
        Long goalId = 1L;
        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setAchievements(new ArrayList<>());

        AchievementRequestDto requestDto = new AchievementRequestDto();
        requestDto.setTitle("Milestone");
        requestDto.setAmountToReach(100f);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        achievementService.addAchievementToGoal(goalId, requestDto);

        verify(achievementRepository).save(argThat(a ->
                a.getTitle().equals("Milestone") &&
                        a.getAmountToReach() == 100f &&
                        a.getGoal() == goal
        ));

        assertEquals(1, goal.getAchievements().size());
    }

    @Test
    void addAchievementToGoal_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        AchievementRequestDto requestDto = new AchievementRequestDto();
        requestDto.setTitle("Test");
        requestDto.setAmountToReach(50f);

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> achievementService.addAchievementToGoal(goalId, requestDto));
        verify(achievementRepository, never()).save(any());
    }

    @Test
    void deleteAchievementFromGoal_ShouldDeleteAchievement_WhenValid() {
        Long goalId = 1L;
        Long achievementId = 10L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setAchievements(new ArrayList<>());

        Achievement achievement = new Achievement();
        achievement.setAchievementId(achievementId);
        achievement.setGoal(goal);

        goal.getAchievements().add(achievement);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));

        achievementService.deleteAchievementFromGoal(goalId, achievementId);

        verify(goalRepository).save(goal);
        verify(achievementRepository).delete(achievement);
        assertFalse(goal.getAchievements().contains(achievement));
    }

    @Test
    void deleteAchievementFromGoal_ShouldThrow_WhenGoalNotFound() {
        when(goalRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                achievementService.deleteAchievementFromGoal(1L, 10L));
        verify(achievementRepository, never()).findById(any());
        verify(achievementRepository, never()).delete(any());
    }

    @Test
    void deleteAchievementFromGoal_ShouldThrowIfAchievementNotFound() {
        Goal goal = new Goal();
        goal.setGoalId(1L);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(achievementRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                achievementService.deleteAchievementFromGoal(1L, 10L));

        verify(achievementRepository, never()).delete(any());
    }

    @Test
    void deleteAchievementFromGoal_ShouldThrow_WhenAchievementDoesNotBelongToGoal() {
        Goal goal = new Goal();
        goal.setGoalId(1L);

        Goal otherGoal = new Goal();
        otherGoal.setGoalId(2L);

        Achievement achievement = new Achievement();
        achievement.setAchievementId(10L);
        achievement.setGoal(otherGoal);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(achievementRepository.findById(10L)).thenReturn(Optional.of(achievement));

        assertThrows(IllegalArgumentException.class, () ->
                achievementService.deleteAchievementFromGoal(1L, 10L));
        verify(achievementRepository, never()).delete(any());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void checkAchievements_ShouldSaveAchievement_WhenCriteriaMet() {
        Long goalId = 1L;
        LocalDate sessionDate = LocalDate.of(2025, 5, 20);

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setCurrentAmount(100f);

        Achievement a1 = new Achievement();
        a1.setAmountToReach(50f);
        a1.setDateAwarded(null);

        Achievement a2 = new Achievement();
        a2.setAmountToReach(120f);
        a2.setDateAwarded(null);

        SessionRequestDto dto = new SessionRequestDto();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(achievementRepository.findByGoal_GoalId(goalId)).thenReturn(List.of(a1, a2));

        achievementService.checkAchievements(goalId, dto);

        assertEquals(sessionDate, a1.getDateAwarded());
        assertNull(a2.getDateAwarded());

        verify(achievementRepository).save(a1);
        verify(achievementRepository, never()).save(a2);
    }

    @Test
    void checkAchievements_ShouldDoNothingIfNoAchievements() {
        Long goalId = 1L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);

        SessionRequestDto dto = new SessionRequestDto();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(achievementRepository.findByGoal_GoalId(goalId)).thenReturn(Collections.emptyList());

        achievementService.checkAchievements(goalId, dto);

        verify(achievementRepository, never()).save(any());
    }

    @Test
    void checkAchievements_ShouldSkipAlreadyAwardedAchievements() {
        Long goalId = 1L;

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setCurrentAmount(100f);

        Achievement awarded = new Achievement();
        awarded.setAmountToReach(90f);
        awarded.setDateAwarded(LocalDate.of(2023, 1, 1)); // Already awarded

        SessionRequestDto dto = new SessionRequestDto();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(achievementRepository.findByGoal_GoalId(goalId)).thenReturn(List.of(awarded));

        achievementService.checkAchievements(goalId, dto);

        verify(achievementRepository, never()).save(any());
    }

    @Test
    void checkAchievements_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        SessionRequestDto dto = new SessionRequestDto();

        assertThrows(EntityNotFoundException.class,
                () -> achievementService.checkAchievements(goalId, dto));

        verify(achievementRepository, never()).findByGoal_GoalId(any());
    }
}