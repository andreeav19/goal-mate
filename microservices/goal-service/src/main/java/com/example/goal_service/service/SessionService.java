package com.example.goal_service.service;

import com.example.goal_service.dto.SessionRequestDto;
import com.example.goal_service.model.Goal;
import com.example.goal_service.model.Session;
import com.example.goal_service.repository.GoalRepository;
import com.example.goal_service.repository.SessionRepository;
import com.example.goal_service.util.UtilLogger;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final GoalRepository goalRepository;
    private final AchievementService achievementService;

    public boolean addSessionToGoal(Long goalId, SessionRequestDto requestDto) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
            UtilLogger.logWarningMessage("Session not added. Target amount is already reached.");
            return false;
        }

        Session session = new Session();
        session.setDate(LocalDate.now());
        session.setProgressAmount(requestDto.getProgressAmount());
        session.setGoal(goal);

        if (goal.getSessions() == null) {
            goal.setSessions(new ArrayList<>());
        }
        goal.getSessions().add(session);
        goal.setCurrentAmount(goal.getCurrentAmount() + requestDto.getProgressAmount());

        sessionRepository.save(session);
        goalRepository.save(goal);

        return achievementService.checkAchievements(goalId, requestDto);
    }

    public void deleteSessionFromGoal(Long goalId, Long sessionId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        Session session = sessionRepository.findById(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Session not found."));

        if (!session.getGoal().getGoalId().equals(goalId)) {
            String message = "Session does not belong to specified goal.";
            UtilLogger.logErrorMessage(message);
            throw new IllegalArgumentException(message);
        }

        if (session.getDate().isAfter(goal.getDeadline())) {
            String message = "Session date cannot be later than goal deadline.";
            UtilLogger.logErrorMessage(message);
            throw new RuntimeException(message);
        }

        goal.setCurrentAmount(
                goal.getCurrentAmount() - session.getProgressAmount()
        );
        goal.getSessions().remove(session);
        goalRepository.save(goal);

        sessionRepository.delete(session);
    }
}

