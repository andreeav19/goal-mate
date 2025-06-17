package com.example.auth_service.controller;

import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.dto.UserRoleRequestDto;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.UserService;
import com.example.auth_service.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "email") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Page<UserResponseDto> userPage = userService.getAllUsers(principal.getName(), page, size, sortBy, sortDir);

        Map<String, Object> response = new HashMap<>();
        response.put("isAdmin", authService.isCurrentUserAdmin());
        response.put("users", userPage.getContent());
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", userPage.getTotalPages());
        response.put("sortBy", sortBy);
        response.put("sortDir", sortDir);
        response.put("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-role")
    public ResponseEntity<?> addRole(@Valid @RequestBody UserRoleRequestDto requestDto,
                                     BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not add role to user.");
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        userService.addUserRole(requestDto);
        return ResponseEntity.ok("Role added successfully.");
    }

    @PostMapping("/delete-role")
    public ResponseEntity<?> deleteRole(@Valid @RequestBody UserRoleRequestDto requestDto,
                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not delete role from user.");
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        userService.deleteUserRole(requestDto);
        return ResponseEntity.ok("Role deleted successfully.");
    }
}
