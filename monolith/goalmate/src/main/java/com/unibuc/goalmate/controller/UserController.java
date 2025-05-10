package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.UserService;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final UserService userService;

    @GetMapping()
    public String getAllUsers(Model model, Principal principal) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("users", userService.getAllUsers(principal.getName()));
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
}
