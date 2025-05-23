package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.AchievementRequestDto;
import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Achievement;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.repository.AchievementRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public void checkAchievements(Long goalId, SessionRequestDto requestDto) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        List<Achievement> achievements = achievementRepository.findByGoal_GoalId(goalId);
        if (achievements.isEmpty()) {
            return;
        }

        for (Achievement achievement : achievements) {
            if (achievement.getDateAwarded() != null) {
                continue;
            }

            if (goal.getCurrentAmount() >= achievement.getAmountToReach()) {
                achievement.setDateAwarded(LocalDate.now());
                achievementRepository.save(achievement);
            }
        }
    }
}
