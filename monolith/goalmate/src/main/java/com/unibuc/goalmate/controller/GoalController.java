package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.HobbyService;
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
    private final HobbyService hobbyService;

    @GetMapping("/goals")
    public String getGoals(Model model, Principal principal) {
        String userEmail = principal.getName();
        model.addAttribute("goals", goalService.getGoalsByLoggedUser(userEmail));
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());

        return "home/goal_page";
    }

    @GetMapping("/goals/add")
    public String getAddGoalPage(Model model/*, Principal principal*/) {
//        String userEmail = principal.getName();
//        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("hobbies", hobbyService.getHobbyOptions());
        return "home/add_goal_page";
    }
}
