package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.SessionService;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/home/goals/{id}/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final GoalService goalService;
    private final AuthService authService;

    @GetMapping()
    public String getGoalSessions(@PathVariable Long id,
                                  Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "date") String sortBy,
                                  @RequestParam(defaultValue = "asc") String sortDir) {

        model.addAttribute("goalSessions", goalService.getGoalSessions(id, page, size, sortBy, sortDir));
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "sessions/session_page";
    }

    @GetMapping("/add")
    public String getAddSessionPage(@PathVariable Long id, Model model) {
        return addSessionModelAttributes(id, model);
    }

    @PostMapping("/add")
    public String addSession(@PathVariable Long id, Model model,
                             @Valid @ModelAttribute("sessionRequest") SessionRequestDto requestDto,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(
                    bindingResult, "Could not add session to goal with id " + id);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return addSessionModelAttributes(id, model);
        }

        sessionService.addSessionToGoal(id, requestDto);
        return "redirect:/home/goals/" + id + "/sessions";
    }

    @PostMapping("/delete/{sessionId}")
    public String deleteSession(@PathVariable Long id, @PathVariable Long sessionId) {
        sessionService.deleteSessionFromGoal(id, sessionId);
        return "redirect:/home/goals/" + id + "/sessions";
    }

    private String addSessionModelAttributes(@PathVariable Long id, Model model) {
        model.addAttribute("sessionRequest", new SessionRequestDto());
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("goalId", id);
        model.addAttribute("goalDeadline", goalService.getGoalDeadline(id));
        model.addAttribute("goalTarget", goalService.getGoalTargetAmount(id));
        model.addAttribute("today", LocalDate.now());

        return "sessions/add_session_page";
    }
}
