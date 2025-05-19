package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.*;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final GoalMateUserRepository userRepository;
    private final HobbyRepository hobbyRepository;

    public List<GoalResponseDto> getGoalsByLoggedUser(String userEmail) {
        return goalRepository.findByUser_Email(userEmail).stream()
                .map(goal -> new GoalResponseDto(
                        goal.getGoalId(),
                        goal.getHobby().getHobbyId(),
                        goal.getHobby().getName(),
                        goal.getDescription(),
                        goal.getTargetAmount(),
                        goal.getCurrentAmount(),
                        goal.getTargetUnit(),
                        goal.getDeadline()
                ))
                .collect(Collectors.toList());
    }

    public void addGoalToLoggedUser(GoalRequestDto goalRequestDto, String userEmail) {
        GoalMateUser user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        Hobby hobby = hobbyRepository.findById(goalRequestDto.getHobbyId()).orElseThrow(
                () -> new EntityNotFoundException("Hobby not found.")
        );

        Goal goal = new Goal();
        goal.setDescription(goalRequestDto.getDescription());
        goal.setTargetAmount(goalRequestDto.getTargetAmount());
        goal.setTargetUnit(goalRequestDto.getUnit());
        goal.setDeadline(goalRequestDto.getDeadline());
        goal.setHobby(hobby);
        goal.setUser(user);

        if (hobby.getGoals() == null) {
            hobby.setGoals(new ArrayList<>());
        }
        if (user.getGoals() == null) {
            user.setGoals(new ArrayList<>());
        }

        user.getGoals().add(goal);
        hobby.getGoals().add(goal);

        goalRepository.save(goal);
    }

    public GoalResponseDto getGoalById(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        return new GoalResponseDto(
                goal.getGoalId(),
                goal.getHobby().getHobbyId(),
                goal.getHobby().getName(),
                goal.getDescription(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetUnit(),
                goal.getDeadline()
        );
    }

    public void editGoal(Long goalId, GoalRequestDto goalRequestDto) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        goal.setDescription(goalRequestDto.getDescription());
        goal.setTargetAmount(goalRequestDto.getTargetAmount());
        goal.setTargetUnit(goalRequestDto.getUnit());
        goal.setDeadline(goalRequestDto.getDeadline());

        Hobby newHobby = hobbyRepository.findById(goalRequestDto.getHobbyId()).orElseThrow(
                () -> new EntityNotFoundException("Hobby not found."));

        Hobby oldHobby = goal.getHobby();

        if (!newHobby.equals(oldHobby)) {
            oldHobby.getGoals().remove(goal);
            if (newHobby.getGoals() == null) {
                newHobby.setGoals(new ArrayList<>());
            }
            newHobby.getGoals().add(goal);
            goal.setHobby(newHobby);
        }

        goalRepository.save(goal);
    }

    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found."));

        Hobby hobby = goal.getHobby();
        hobby.getGoals().remove(goal);
        hobbyRepository.save(hobby);

        goalRepository.delete(goal);
    }

    public GoalSessionsResponseDto getGoalSessions(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        return new GoalSessionsResponseDto(
                goalId,
                goal.getHobby().getName(),
                goal.getTargetUnit(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getSessions().stream().map(
                        session -> new SessionResponseDto(
                                session.getSessionId(),
                                session.getDate(),
                                session.getProgressAmount()
                        )
                ).toList()
        );
    }

    public GoalAchievementsResponseDto getGoalAchievements(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        return new GoalAchievementsResponseDto(
                goalId,
                goal.getHobby().getName(),
                goal.getTargetUnit(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getAchievements().stream().map(
                        achievement -> new AchievementResponseDto(
                                achievement.getAchievementId(),
                                achievement.getTitle(),
                                achievement.getAmountToReach(),
                                achievement.getDateAwarded()
                        )
                ).toList()
        );
    }

    public LocalDate getGoalDeadline(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        return goal.getDeadline();
    }

    public Float getGoalTargetAmount(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        return goal.getTargetAmount();
    }

    public Float getGoalCurrentAmount(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        return goal.getCurrentAmount();
    }
}
