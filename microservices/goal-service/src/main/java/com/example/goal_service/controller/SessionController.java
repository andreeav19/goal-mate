package com.example.goal_service.controller;

import com.example.goal_service.dto.SessionRequestDto;
import com.example.goal_service.client.AuthClient;
import com.example.goal_service.service.GoalService;
import com.example.goal_service.service.SessionService;
import com.example.goal_service.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/goals/{id}/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final GoalService goalService;
    private final AuthClient authClient;

    @GetMapping
    public ResponseEntity<?> getGoalSessions(@PathVariable Long id,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "date") String sortBy,
                                             @RequestParam(defaultValue = "asc") String sortDir) {

        Map<String, Object> response = new HashMap<>();
        response.put("today", LocalDate.now());
        response.put("goalSessions", goalService.getGoalSessions(id, page, size, sortBy, sortDir));

        boolean isAdmin = false;
        try {
            isAdmin = authClient.isCurrentUserAdmin();
        } catch (Exception e) {
            UtilLogger.logErrorMessage(e.getMessage());
        }
        response.put("isAdmin", isAdmin);

        response.put("sortBy", sortBy);
        response.put("sortDir", sortDir);
        response.put("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addSession(@PathVariable Long id,
                                        @RequestBody @Valid SessionRequestDto requestDto,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not add session to goal with id " + id);
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        boolean unlocked = sessionService.addSessionToGoal(id, requestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Session added successfully.");
        response.put("achievementUnlocked", unlocked);

        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id, @PathVariable Long sessionId) {
        sessionService.deleteSessionFromGoal(id, sessionId);
        return ResponseEntity.noContent().build();
    }
}
