package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.AchievementRequestDto;
import com.unibuc.goalmate.model.Achievement;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.repository.AchievementRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final GoalRepository goalRepository;

    public void addAchievementToGoal(Long goalId, AchievementRequestDto requestDto) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        Achievement achievement = new Achievement();
        achievement.setTitle(requestDto.getTitle());
        achievement.setAmountToReach(requestDto.getAmountToReach());
        achievement.setDateAwarded(requestDto.getDateAwarded());
        achievement.setGoal(goal);

        if (goal.getAchievements() == null) {
            goal.setAchievements(new ArrayList<>());
        }
        goal.getAchievements().add(achievement);
        achievementRepository.save(achievement);
    }

    public void deleteAchievementFromGoal(Long goalId, Long achievementId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        Achievement achievement = achievementRepository.findById(achievementId).orElseThrow(
                () -> new EntityNotFoundException("Achievement not found."));

        if (!achievement.getGoal().getGoalId().equals(goalId)) {
            String message = "Achievement does not belong to specified goal.";
            UtilLogger.logErrorMessage(message);
            throw new IllegalArgumentException(message);
        }

        goal.getAchievements().remove(achievement);
        goalRepository.save(goal);

        achievementRepository.delete(achievement);
    }
}
