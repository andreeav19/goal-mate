package com.unibuc.goalmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class GoalController {

    @GetMapping("/goals")
    public String getGoals() {
        return "home/goal_page";
    }
}
