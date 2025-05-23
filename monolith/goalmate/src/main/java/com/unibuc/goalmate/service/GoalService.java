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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final GoalMateUserRepository userRepository;
    private final HobbyRepository hobbyRepository;

    public Page<GoalResponseDto> getGoalsByLoggedUser(String userEmail, int page, int size, String sortBy, String sortDir, String filterStatus) {
        List<Goal> allGoals = goalRepository.findByUser_Email(userEmail);
        LocalDate today = LocalDate.now();

        // Mapping + calcul status
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

                    return GoalResponseDto.builder()
                            .goalId(goal.getGoalId())
                            .hobbyId(goal.getHobby().getHobbyId())
                            .hobbyName(goal.getHobby().getName())
                            .description(goal.getDescription())
                            .targetAmount(goal.getTargetAmount())
                            .currentAmount(goal.getCurrentAmount())
                            .unit(goal.getTargetUnit())
                            .deadline(goal.getDeadline())
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());

        // Filtrare după status
        if (filterStatus != null && !filterStatus.isEmpty()) {
            goalDtos = goalDtos.stream()
                    .filter(dto -> dto.getStatus().equalsIgnoreCase(filterStatus))
                    .collect(Collectors.toList());
        }

        // Sortare
        Comparator<GoalResponseDto> comparator = Comparator.comparing(GoalResponseDto::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));

        if ("hobbyName".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(GoalResponseDto::getHobbyName, String.CASE_INSENSITIVE_ORDER);
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        goalDtos.sort(comparator);

        // Paginare
        int start = page * size;
        int end = Math.min(start + size, goalDtos.size());
        List<GoalResponseDto> paginated = (start < end) ? goalDtos.subList(start, end) : new ArrayList<>();

        return new PageImpl<>(paginated, PageRequest.of(page, size), goalDtos.size());
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

        return GoalResponseDto.builder()
                .goalId(goal.getGoalId())
                .hobbyId(goal.getHobby().getHobbyId())
                .hobbyName(goal.getHobby().getName())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .unit(goal.getTargetUnit())
                .deadline(goal.getDeadline())
                .build();
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
            if (oldHobby.getGoals() == null) {
                oldHobby.setGoals(new ArrayList<>());
            }
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

    public String getGoalUnit(Long goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(
                () -> new EntityNotFoundException("Goal not found.")
        );

        return goal.getTargetUnit();
    }
}
