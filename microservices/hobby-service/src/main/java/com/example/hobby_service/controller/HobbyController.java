package com.example.hobby_service.controller;

import com.example.hobby_service.dto.HobbyRequestDto;
import com.example.hobby_service.service.HobbyService;
import com.example.hobby_service.util.UtilLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hobbies")
@RequiredArgsConstructor
public class HobbyController {

    private final HobbyService hobbyService;

    @GetMapping
    public ResponseEntity<?> getAllHobbies(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "8") int size,
                                           @RequestParam(defaultValue = "name") String sortBy,
                                           @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(hobbyService.getAllHobbies(page, size, sortBy, sortDir));
    }

    @PostMapping
    public ResponseEntity<?> addHobby(@Valid @RequestBody HobbyRequestDto requestDto,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            UtilLogger.logBindingResultErrors(bindingResult, "Could not add hobby.");
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            hobbyService.addHobby(requestDto);
            return ResponseEntity.ok("Hobby added successfully.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteHobby(@RequestParam String hobbyName) {
        hobbyService.deleteHobbyByName(hobbyName);
        return ResponseEntity.ok("Hobby deleted successfully.");
    }
}

