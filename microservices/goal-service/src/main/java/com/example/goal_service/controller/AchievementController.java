package com.example.goal_service.controller;

import com.example.goal_service.dto.AchievementRequestDto;
import com.example.goal_service.client.AuthClient;
import com.example.goal_service.service.AchievementService;
import com.example.goal_service.service.GoalService;
import com.example.goal_service.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/goals/{id}/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final GoalService goalService;
    private final AuthClient authClient;

    @GetMapping
    public ResponseEntity<?> getGoalAchievements(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("today", LocalDate.now());
        response.put("goalAchievements", goalService.getGoalAchievements(id));

        boolean isAdmin = false;
        try {
            isAdmin = authClient.isCurrentUserAdmin();
        } catch (Exception e) {
            UtilLogger.logErrorMessage(e.getMessage());
        }
        response.put("isAdmin", isAdmin);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addAchievement(@PathVariable Long id,
                                            @RequestBody @Valid AchievementRequestDto requestDto,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not add achievement to goal with id " + id);
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        achievementService.addAchievementToGoal(id, requestDto);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{achievementId}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Long id, @PathVariable Long achievementId) {
        achievementService.deleteAchievementFromGoal(id, achievementId);
        return ResponseEntity.noContent().build();
    }
}
