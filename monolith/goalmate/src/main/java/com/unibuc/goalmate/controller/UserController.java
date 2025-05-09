package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.RoleService;
import com.unibuc.goalmate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final UserService userService;
    private final RoleService roleService;

    @GetMapping()
    public String getAllUsers(Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/users";
    }
}
