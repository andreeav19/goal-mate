package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.GoalRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.HobbyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

@Controller
@RequestMapping("/home/goals")
@RequiredArgsConstructor
public class GoalController {
    private static final Logger logger = LoggerFactory.getLogger(GoalController.class);

    private final AuthService authService;
    private final GoalService goalService;
    private final HobbyService hobbyService;

    @GetMapping()
    public String getGoals(Model model, Principal principal) {
        String userEmail = principal.getName();
        model.addAttribute("goals", goalService.getGoalsByLoggedUser(userEmail));
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());

        return "home/goal_page";
    }

    @GetMapping("/add")
    public String getAddGoalPage(Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("hobbies", hobbyService.getHobbyOptions());
        model.addAttribute("goalRequest", new GoalRequestDto());
        return "home/add_goal_page";
    }

    @PostMapping("/add")
    public String addGoal(@ModelAttribute("goalRequest") @Valid GoalRequestDto request,
                          Principal principal, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/home/goals/add?error";
        }

        goalService.addGoalToLoggedUser(request, principal.getName());
        return "redirect:/home/goals";
    }

    @GetMapping("/{id}")
    public String getEditGoalPage(@PathVariable Long id, Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("goalRequest", goalService.getGoalById(id));
        model.addAttribute("hobbies", hobbyService.getHobbyOptions());

        return "home/edit_goal_page";
    }

    @PostMapping("/edit/{id}")
    public String editGoal(@PathVariable Long id,
                           @ModelAttribute("goalRequest") @Valid GoalRequestDto request,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while editing goal with ID {}:", id);
            bindingResult.getAllErrors().forEach(error ->
                    logger.warn(" - {}", error.getDefaultMessage())
            );
            return "redirect:/home/goals/edit";
        }

        goalService.editGoal(id, request);
        logger.info("Successfully edited goal with ID {}", id);
        return  "redirect:/home/goals";
    }

    @PostMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return  "redirect:/home/goals";
    }
}
