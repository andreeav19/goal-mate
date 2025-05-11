package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.Session;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.SessionRepository;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final GoalRepository goalRepository;

    public void addSessionToGoal(Long goalId, SessionRequestDto requestDto) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
            UtilLogger.logWarningMessage("Session not added. Target amount is already reached.");
            return;
        }

        Session session = new Session();
        session.setDate(requestDto.getDate());
        session.setProgressAmount(requestDto.getProgressAmount());
        session.setGoal(goal);

        if (goal.getSessions() == null) {
            goal.setSessions(new ArrayList<>());
        }
        goal.getSessions().add(session);
        goal.setCurrentAmount(
                goal.getCurrentAmount() + requestDto.getProgressAmount()
        );

        sessionRepository.save(session);
        goalRepository.save(goal);
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
