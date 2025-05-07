package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.LoginRequestDto;
import com.unibuc.goalmate.dto.LoginResponseDto;
import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//@RestController
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute("message", "hello");
        return "auth/register";
    }

//    @PostMapping("/register")
//    public String register(@RequestBody @Valid RegisterRequestDto request) {
//        return authService.register(request);
//    }
//
//    @PostMapping("/login")
//    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
//        return authService.login(request);
//    }
}
