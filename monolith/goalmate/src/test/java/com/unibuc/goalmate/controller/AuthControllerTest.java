package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.HobbyRequestDto;
import com.unibuc.goalmate.dto.RegisterRequestDto;
import com.unibuc.goalmate.security.SecurityConfig;
import com.unibuc.goalmate.security.UserDetailsServiceImpl;
import com.unibuc.goalmate.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private AuthService authService;

    @Test
    void getRegisterPage() throws Exception{
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("registerRequest", instanceOf(RegisterRequestDto.class)));
    }

    @Test
    void postRegister_ValidInput() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto(
                "user@example.com",
                "user",
                "User1!!!");

        mockMvc.perform(post("/auth/register")
                        .flashAttr("registerRequest", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?success"));

        verify(authService, times(1)).register(dto);
    }

    @Test
    void postRegister_InvalidInput() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto();

        mockMvc.perform(post("/auth/register")
                        .flashAttr("registerRequest", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errors"));

        verify(authService, never()).register(dto);
    }

    @Test
    void getLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @WithMockUser
    void getAccessDeniedPage() throws Exception {
        mockMvc.perform(get("/auth/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/access_denied"));
    }
}