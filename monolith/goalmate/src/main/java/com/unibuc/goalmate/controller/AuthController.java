package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerRequest") @Valid RegisterRequestDto request,
                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Error registering new user.");
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "auth/register";
        }

        authService.register(request);
        return "redirect:/auth/login?success";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String getAccessDeniedPage() {
        return "error/access_denied";
    }
}
