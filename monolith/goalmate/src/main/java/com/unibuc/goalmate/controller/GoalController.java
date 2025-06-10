package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.GoalRequestDto;
import com.unibuc.goalmate.dto.GoalResponseDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.HobbyService;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/home/goals")
@RequiredArgsConstructor
public class GoalController {
    private final AuthService authService;
    private final GoalService goalService;
    private final HobbyService hobbyService;

    @GetMapping()
    public String getGoals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String filterStatus,
            Model model,
            Principal principal) {

        String userEmail = principal.getName();

        Page<GoalResponseDto> goalPage = goalService.getGoalsByLoggedUser(userEmail, page, size, sortBy, sortDir, filterStatus);

        model.addAttribute("goals", goalPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", goalPage.getTotalPages());
        model.addAttribute("totalItems", goalPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());

        return "home/goal_page";
    }

    @GetMapping("/add")
    public String getAddGoalPage(Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("hobbies", hobbyService.getHobbyOptions());
        model.addAttribute("goalRequest", new GoalRequestDto());
        model.addAttribute("today", LocalDate.now());

        return "home/add_goal_page";
    }

    @PostMapping("/add")
    public String addGoal(@ModelAttribute("goalRequest") @Valid GoalRequestDto request,
                          BindingResult bindingResult, Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Error while adding goal to logged user.");
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
            model.addAttribute("hobbies", hobbyService.getHobbyOptions());
            model.addAttribute("goalRequest", new GoalRequestDto());
            model.addAttribute("today", LocalDate.now());

            return "home/add_goal_page";
        }

        goalService.addGoalToLoggedUser(request, principal.getName());
        return "redirect:/home/goals";
    }

    @GetMapping("/{id}")
    public String getEditGoalPage(@PathVariable Long id, Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("goalRequest", goalService.getGoalById(id));
        model.addAttribute("hobbies", hobbyService.getHobbyOptions());
        model.addAttribute("today", LocalDate.now());

        return "home/edit_goal_page";
    }

    @PostMapping("/edit/{id}")
    public String editGoal(@PathVariable Long id, Model model,
                           @ModelAttribute("goalRequest") @Valid GoalRequestDto request,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Error while editing goal with id " + id);
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
            model.addAttribute("goalRequest", goalService.getGoalById(id));
            model.addAttribute("hobbies", hobbyService.getHobbyOptions());
            model.addAttribute("today", LocalDate.now());

            return "home/edit_goal_page";
        }

        goalService.editGoal(id, request);
        return "redirect:/home/goals";
    }

    @PostMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return "redirect:/home/goals";
    }
}
