package com.example.goal_service.controller;

import com.example.goal_service.dto.GoalRequestDto;
import com.example.goal_service.dto.GoalResponseDto;
import com.example.goal_service.dto.HobbyOptionDto;
import com.example.goal_service.client.AuthClient;
import com.example.goal_service.client.HobbyClient;
import com.example.goal_service.service.GoalService;
import com.example.goal_service.util.UtilLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final AuthClient authClient;
    private final HobbyClient hobbyClient;
    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<?> getGoals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String filterStatus,
            Principal principal) {

        String userEmail = principal.getName();

        Page<GoalResponseDto> goalPage = goalService.getGoalsByLoggedUser(userEmail, page, size, sortBy, sortDir, filterStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("goals", goalPage.getContent());
        response.put("currentPage", goalPage.getNumber());
        response.put("totalPages", goalPage.getTotalPages());
        response.put("totalItems", goalPage.getTotalElements());
        response.put("sortBy", sortBy);
        response.put("sortDir", sortDir);
        response.put("filterStatus", filterStatus);

        boolean isAdmin = false;
        try {
            isAdmin = authClient.isCurrentUserAdmin();
        } catch (Exception e) {
            UtilLogger.logErrorMessage(e.getMessage());
        }
        response.put("isAdmin", isAdmin);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addGoal(@RequestBody @Valid GoalRequestDto request,
                                     BindingResult bindingResult,
                                     Principal principal) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Error while adding goal to logged user.");
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        goalService.addGoalToLoggedUser(request, principal.getName());
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id) {
        var goalRequest = goalService.getGoalById(id);

        boolean isAdmin = false;
        List<HobbyOptionDto> hobbies = List.of();
        try {
            isAdmin = authClient.isCurrentUserAdmin();
        } catch (Exception e) {
        }
        try {
            hobbies = hobbyClient.getHobbyOptions();
        } catch (Exception e) {
        }

        Map<String, Object> response = new HashMap<>();
        response.put("goalRequest", goalRequest);
        response.put("isAdmin", isAdmin);
        response.put("hobbies", hobbies);
        response.put("today", LocalDate.now());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editGoal(@PathVariable Long id,
                                      @RequestBody @Valid GoalRequestDto request,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Error while editing goal with id " + id);
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        goalService.editGoal(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        return ResponseEntity.ok("Token: " + auth);
    }
}
