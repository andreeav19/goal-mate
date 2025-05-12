package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.AchievementRequestDto;
import com.unibuc.goalmate.model.Achievement;
import com.unibuc.goalmate.service.AchievementService;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.util.UtilLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/home/goals/{id}/achievements")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;
    private final GoalService goalService;
    private final AuthService authService;

    @GetMapping()
    public String getGoalAchievements(@PathVariable Long id, Model model) {
        List<Achievement> achievements = goalService.getGoalAchievements(id);
        model.addAttribute("achievements", achievements);
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        return "achievement/achievement_page";
    }

    @GetMapping("/add")
    public String getAddAchievementPage(@PathVariable Long id, Model model) {
        return addAchievementModelAttributes(id, model);
    }

    @PostMapping("/add")
    public String addAchievement(@PathVariable Long id, Model model,
                                 @ModelAttribute("achievementRequest") AchievementRequestDto requestDto,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(
                    bindingResult, "Could not add achievement to goal with id " + id);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return addAchievementModelAttributes(id, model);
        }

        achievementService.addAchievementToGoal(id, requestDto);
        return "redirect:/home/goals/" + id + "/achievements";
    }

    @PostMapping("/delete/{achievementId}")
    public String deleteAchievement(@PathVariable Long id, @PathVariable Long achievementId) {
        achievementService.deleteAchievementFromGoal(id, achievementId);
        return "redirect:/home/goals/" + id + "/achievements";
    }

    private String addAchievementModelAttributes(@PathVariable Long id, Model model) {
        model.addAttribute("achievementRequest", new AchievementRequestDto());
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("goalId", id);
        model.addAttribute("goalDeadline", goalService.getGoalDeadline(id));
        model.addAttribute("goalTarget", goalService.getGoalTargetAmount(id));

        return "achievements/add_achievement_page";
    }
}
