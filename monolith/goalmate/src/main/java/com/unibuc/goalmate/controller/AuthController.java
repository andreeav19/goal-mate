package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.LoginRequestDto;
import com.unibuc.goalmate.dto.LoginResponseDto;
import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

//@RestController
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
    public String register(@ModelAttribute("registerRequest") @Valid RegisterRequestDto request, BindingResult result) {
        if (result.hasErrors()) {
            System.out.println(result.getAllErrors());
            return "auth/register";
        }

        authService.register(request);
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/hello")
    public String hello() {
        return "auth/hello";
    }
}
