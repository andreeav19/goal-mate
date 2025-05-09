package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class GoalController {
    private final AuthService authService;
    private final GoalService goalService;

    @GetMapping("/goals")
    public String getGoals(Model model, Principal principal) {
        String userEmail = principal.getName();
        model.addAttribute("goals", goalService.getGoalsByLoggedUser(userEmail));
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());

        return "home/goal_page";
    }
}
