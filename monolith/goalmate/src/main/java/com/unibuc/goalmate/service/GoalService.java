package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.GoalResponseDto;
import com.unibuc.goalmate.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;

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
}
