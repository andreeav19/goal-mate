package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class GoalController {
    private final AuthService authService;

    @GetMapping("/goals")
    public String getGoals(Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        return "home/goal_page";
    }
}
