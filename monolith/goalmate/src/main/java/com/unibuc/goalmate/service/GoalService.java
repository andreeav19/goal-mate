package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.GoalRequestDto;
import com.unibuc.goalmate.dto.GoalResponseDto;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
