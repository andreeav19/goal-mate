package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.HobbyRequestDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.HobbyService;
import com.unibuc.goalmate.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hobbies")
@RequiredArgsConstructor
public class HobbyController {
    private final AuthService authService;
    private final HobbyService hobbyService;

    @GetMapping()
    public String getAllHobbies(Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("hobbies", hobbyService.getAllHobbies());
        model.addAttribute("hobbyRequest", new HobbyRequestDto());
        return "hobby/hobbies";
    }

    @GetMapping("/add-hobby")
    public String getAddHobbyPage(Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
        model.addAttribute("hobbyRequest", new HobbyRequestDto());
        return "hobby/add_hobby_page";
    }

    @PostMapping("/add-hobby")
    public String addHobby(@ModelAttribute("hobbyRequest") @Valid HobbyRequestDto requestDto,
                           BindingResult bindingResult,
                           Model model) {
        model.addAttribute("isAdmin", authService.isCurrentUserAdmin());

        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not add hobby.");
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("isAdmin", authService.isCurrentUserAdmin());
            model.addAttribute("hobbyRequest", new HobbyRequestDto());

            return "hobby/add_hobby_page";
        }

        try {
            hobbyService.addHobby(requestDto);
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "hobby/add_hobby_page";
        }

        return "redirect:/hobbies";
    }

    @PostMapping("/delete-hobby")
    public String deleteHobby(@RequestParam String hobbyName) {
        hobbyService.deleteHobbyByName(hobbyName);
        return "redirect:/hobbies";
    }
}
