package com.example.goal_service.controller;

import com.example.goal_service.dto.AchievementRequestDto;
import com.example.goal_service.feign.AuthClient;
import com.example.goal_service.service.AchievementService;
import com.example.goal_service.service.GoalService;
import com.example.goal_service.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
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
            // optional: log warning
        }
        response.put("isAdmin", isAdmin);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/add")
    public ResponseEntity<?> getAddAchievementData(@PathVariable Long id) {
        DecimalFormat df = new DecimalFormat("#.##");
        Map<String, Object> response = new HashMap<>();
        response.put("minAmount", df.format(goalService.getGoalCurrentAmount(id) + 0.01));
        response.put("maxAmount", df.format(goalService.getGoalTargetAmount(id)));
        response.put("unit", goalService.getGoalUnit(id));
        response.put("achievementRequest", new AchievementRequestDto());

        boolean isAdmin = false;
        try {
            isAdmin = authClient.isCurrentUserAdmin();
        } catch (Exception e) {
        }
        response.put("isAdmin", isAdmin);

        response.put("goalId", id);
        response.put("goalTarget", goalService.getGoalTargetAmount(id));
        response.put("goalCurrentAmount", goalService.getGoalCurrentAmount(id));
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
