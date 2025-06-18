package com.example.goal_service.service;

import com.example.goal_service.dto.*;
import com.example.goal_service.feign.AuthClient;
import com.example.goal_service.feign.HobbyClient;
import com.example.goal_service.model.Goal;
import com.example.goal_service.model.Session;
import com.example.goal_service.repository.GoalRepository;
import com.example.goal_service.util.UtilLogger;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final AuthClient authClient;
    private final HobbyClient hobbyClient;

    public Page<GoalResponseDto> getGoalsByLoggedUser(String userEmail, int page, int size, String sortBy, String sortDir, String filterStatus) {
        Long userId = authClient.getUserByEmail(userEmail).getId();

        List<Goal> allGoals = goalRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();

        List<GoalResponseDto> goalDtos = allGoals.stream()
                .map(goal -> {
                    boolean isCompleted = goal.getCurrentAmount() >= goal.getTargetAmount()
                            && (goal.getDeadline() == null || !goal.getDeadline().isBefore(today));
                    boolean isFailed = goal.getCurrentAmount() < goal.getTargetAmount()
                            && goal.getDeadline() != null
                            && goal.getDeadline().isBefore(today);

                    String status;
                    if (isCompleted) status = "completed";
                    else if (isFailed) status = "failed";
                    else status = "inprogress";

                    HobbyDto hobbyDto = hobbyClient.getHobbyById(goal.getHobbyId());
                    String hobbyName = hobbyDto != null ? hobbyDto.getHobbyName() : "Unknown";

                    return GoalResponseDto.builder()
                            .goalId(goal.getGoalId())
                            .hobbyId(goal.getHobbyId())
                            .hobbyName(hobbyName)
                            .description(goal.getDescription())
                            .targetAmount(goal.getTargetAmount())
                            .currentAmount(goal.getCurrentAmount())
                            .unit(goal.getTargetUnit())
                            .deadline(goal.getDeadline())
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());

        if (filterStatus != null && !filterStatus.isEmpty()) {
            goalDtos = goalDtos.stream()
                    .filter(dto -> dto.getStatus().equalsIgnoreCase(filterStatus))
                    .collect(Collectors.toList());
        }

        Comparator<GoalResponseDto> comparator = Comparator.comparing(GoalResponseDto::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("hobbyName".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(GoalResponseDto::getHobbyName, String.CASE_INSENSITIVE_ORDER);
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        goalDtos.sort(comparator);

        int start = page * size;
        int end = Math.min(start + size, goalDtos.size());
        List<GoalResponseDto> paginated = (start < end) ? goalDtos.subList(start, end) : new ArrayList<>();

        return new PageImpl<>(paginated, PageRequest.of(page, size), goalDtos.size());
    }

    public void addGoalToLoggedUser(GoalRequestDto goalRequestDto, String userEmail) {
        GoalMateUserDto userDto = authClient.getUserByEmail(userEmail);
        if (userDto == null) {
            throw new EntityNotFoundException("User not found in auth-service.");
        }

        HobbyDto hobbyDto = hobbyClient.getHobbyById(goalRequestDto.getHobbyId());
        if (hobbyDto == null) {
            UtilLogger.logErrorMessage("Hobby not found in hobby-service.");
            throw new EntityNotFoundException("Hobby not found in hobby-service.");
        }

        if (hobbyDto.getId() == null) {
            UtilLogger.logErrorMessage("HobbyId is null");
        }

        Goal goal = new Goal();
        goal.setDescription(goalRequestDto.getDescription());
        goal.setTargetAmount(goalRequestDto.getTargetAmount());
        goal.setTargetUnit(goalRequestDto.getUnit());
        goal.setDeadline(goalRequestDto.getDeadline());
        goal.setUserId(userDto.getId());
        goal.setHobbyId(hobbyDto.getId());

        goalRepository.save(goal);
    }


    public GoalResponseDto getGoalById(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."));

        HobbyDto hobbyDto = hobbyClient.getHobbyById(goal.getHobbyId());
        String hobbyName = hobbyDto != null ? hobbyDto.getHobbyName() : "Unknown";

        return GoalResponseDto.builder()
                .goalId(goal.getGoalId())
                .hobbyId(goal.getHobbyId())
                .hobbyName(hobbyName)
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .unit(goal.getTargetUnit())
                .deadline(goal.getDeadline())
                .build();
    }

    public void editGoal(Long goalId, GoalRequestDto goalRequestDto) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."));

        HobbyDto newHobbyDto = hobbyClient.getHobbyById(goalRequestDto.getHobbyId());
        if (newHobbyDto == null) {
            throw new EntityNotFoundException("Hobby not found.");
        }

        goal.setDescription(goalRequestDto.getDescription());
        goal.setTargetAmount(goalRequestDto.getTargetAmount());
        goal.setTargetUnit(goalRequestDto.getUnit());
        goal.setDeadline(goalRequestDto.getDeadline());
        goal.setHobbyId(newHobbyDto.getId());

        goalRepository.save(goal);
    }


    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."));
        goalRepository.delete(goal);
    }

    public GoalSessionsResponseDto getGoalSessions(Long goalId, int page, int size, String sortBy, String sortDir) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."));

        List<Session> sessions = new ArrayList<>(goal.getSessions());

        Comparator<Session> comparator;
        switch (sortBy) {
            case "date" -> comparator = Comparator.comparing(Session::getDate);
            case "progressAmount" -> comparator = Comparator.comparing(Session::getProgressAmount);
            default -> comparator = Comparator.comparing(Session::getSessionId);
        }

        if (sortDir.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        sessions.sort(comparator);

        int totalSessions = sessions.size();
        int start = page * size;
        int end = Math.min(start + size, sessions.size());
        List<Session> paginatedSessions = (start < end) ? sessions.subList(start, end) : Collections.emptyList();

        List<SessionResponseDto> sessionDtos = paginatedSessions.stream()
                .map(session -> new SessionResponseDto(
                        session.getSessionId(),
                        session.getDate(),
                        session.getProgressAmount()
                ))
                .toList();

        boolean hasNext = (page + 1) * size < totalSessions;

        HobbyDto hobbyDto = hobbyClient.getHobbyById(goal.getHobbyId());
        String hobbyName = hobbyDto != null ? hobbyDto.getHobbyName() : "Unknown";

        return new GoalSessionsResponseDto(
                goalId,
                hobbyName,
                goal.getTargetUnit(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                sessionDtos,
                hasNext
        );
    }

    public GoalAchievementsResponseDto getGoalAchievements(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."));

        HobbyDto hobbyDto = hobbyClient.getHobbyById(goal.getHobbyId());
        String hobbyName = hobbyDto != null ? hobbyDto.getHobbyName() : "Unknown";

        return new GoalAchievementsResponseDto(
                goalId,
                hobbyName,
                goal.getTargetUnit(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getAchievements().stream()
                        .map(achievement -> new AchievementResponseDto(
                                achievement.getAchievementId(),
                                achievement.getTitle(),
                                achievement.getAmountToReach(),
                                achievement.getDateAwarded()
                        ))
                        .toList()
        );
    }

    public LocalDate getGoalDeadline(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."))
                .getDeadline();
    }

    public Float getGoalTargetAmount(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."))
                .getTargetAmount();
    }

    public Float getGoalCurrentAmount(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."))
                .getCurrentAmount();
    }

    public String getGoalUnit(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found."))
                .getTargetUnit();
    }
}