package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.SessionService;
import com.unibuc.goalmate.util.UtilLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/home/goals/{id}/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final GoalService goalService;
    private final AuthService authService;

    @GetMapping()
    public String getGoalSessions(@PathVariable Long id, Model model) {
        model.addAttribute("goalSessions", goalService.getGoalSessions(id));
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        return "sessions/session_page";
    }

    @GetMapping("/add")
    public String getAddSessionPage(@PathVariable Long id, Model model) {
        model.addAttribute("sessionRequest", new SessionRequestDto());
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("goalId", id);
        model.addAttribute("sessionRequest", new SessionRequestDto());
        return "sessions/add_session_page";
    }

    @PostMapping("/add")
    public String addSession(@PathVariable Long id,
                             @ModelAttribute("sessionRequest") SessionRequestDto requestDto,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(
                    bindingResult, "Could not add session to goal with id " + id);
            return "redirect:/home/goals/" + id + "/sessions";
        }

        sessionService.addSessionToGoal(id, requestDto);
        return "redirect:/home/goals/" + id + "/sessions";
    }

    @PostMapping("/delete/{sessionId}")
    public String deleteSession(@PathVariable Long id, @PathVariable Long sessionId) {
        sessionService.deleteSessionFromGoal(id, sessionId);
        return "redirect:/home/goals/" + id + "/sessions";
    }
}
