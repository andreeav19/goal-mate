package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.UserService;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final UserService userService;

    @GetMapping()
    public String getAllUsers(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "email") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());

        Page<UserResponseDto> userPage = userService.getAllUsers(principal.getName(), page, size, sortBy, sortDir);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("userRoleRequest", new UserRoleRequestDto());

        return "admin/users";
    }


    @PostMapping("/add-role")
    public String addRole(@ModelAttribute("userRoleRequest") @Valid UserRoleRequestDto requestDto,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not add role to user.");
            return "redirect:/admin";
        }

        userService.addUserRole(requestDto);
        return "redirect:/admin";
    }

    @PostMapping("/delete-role")
    public String deleteRole(@ModelAttribute("userRoleRequest") @Valid UserRoleRequestDto requestDto,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not delete role from user.");
            return "redirect:/admin";
        }

        userService.deleteUserRole(requestDto);
        return "redirect:/admin";
    }
}
