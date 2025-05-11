package com.unibuc.goalmate.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorControllerAdvice {
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error_page";
    }
}
