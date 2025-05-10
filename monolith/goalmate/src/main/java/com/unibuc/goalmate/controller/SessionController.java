package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home/goals")
@RequiredArgsConstructor
public class SessionController {
    private final GoalService goalService;
    private final AuthService authService;

    @GetMapping("/{id}/sessions")
    public String getGoalSessions(@PathVariable Long id, Model model) {
        model.addAttribute("goalSessions", goalService.getGoalSessions(id));
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        return "sessions/session_page";
    }
}
