package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.advice.ErrorControllerAdvice;
import com.unibuc.goalmate.dto.GoalSessionsResponseDto;
import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.dto.SessionResponseDto;
import com.unibuc.goalmate.security.SecurityConfig;
import com.unibuc.goalmate.security.UserDetailsServiceImpl;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@Import({SecurityConfig.class, ErrorControllerAdvice.class})
@WebMvcTest(SessionController.class)
class SessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private GoalService goalService;

    @Test
    @WithMockUser
    void getGoalSessions_ValidId_ShouldReturnSessionsPage() throws Exception {
        Long goalId = 1L;
        GoalSessionsResponseDto response = new GoalSessionsResponseDto(
                goalId,
                "Drawing",
                "paintings",
                10f,
                2f,
                List.of(new SessionResponseDto(1L, LocalDate.now(), 2f))
        );

        when(goalService.getGoalSessions(goalId)).thenReturn(response);
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(get("/home/goals/1/sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/session_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("goalSessions", response));
    }

    @Test
    @WithMockUser
    void getAddSessions_ValidId_ShouldReturnAddSessionPage() throws Exception {
        Long goalId = 1L;
        LocalDate goalDeadline = LocalDate.now().plusMonths(1);
        when(goalService.getGoalDeadline(goalId)).thenReturn(goalDeadline);
        when(goalService.getGoalTargetAmount(goalId)).thenReturn(10f);
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(get("/home/goals/1/sessions/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/add_session_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("sessionRequest", instanceOf(SessionRequestDto.class)))
                .andExpect(model().attribute("goalId", goalId))
                .andExpect(model().attribute("goalDeadline", goalDeadline))
                .andExpect(model().attribute("goalTarget", 10f))
                .andExpect(model().attribute("today", LocalDate.now()));

    }

    @Test
    @WithMockUser
    void postAddSession_ValidInput_ShouldRedirectSessionsPage() throws Exception {
        SessionRequestDto dto = new SessionRequestDto(LocalDate.now(), 1f);

        mockMvc.perform(post("/home/goals/1/sessions/add")
                .flashAttr("sessionRequest", dto)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals/1/sessions"));

        verify(sessionService, times(1)).addSessionToGoal(1L, dto);
    }

    @Test
    @WithMockUser
    void postAddSession_InvalidInput_ShouldRedirectToAddSessionPage() throws Exception {
        SessionRequestDto dto = new SessionRequestDto(null, null);

        mockMvc.perform(post("/home/goals/1/sessions/add")
                        .flashAttr("sessionRequest", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/add_session_page"))
        ;

        verify(sessionService, times(0)).addSessionToGoal(1L, dto);
    }

    @Test
    @WithMockUser
    void postDeleteSession_ValidInput_ShouldRedirectSessionsPage() throws Exception {
        mockMvc.perform(post("/home/goals/1/sessions/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals/1/sessions"));

        verify(sessionService, times(1)).deleteSessionFromGoal(1L, 1L);
    }

    @Test
    @WithMockUser
    void postDeleteSession_InvalidInput_ShouldRedirectErrorPage() throws Exception {
        doThrow(new RuntimeException("Something went wrong."))
                .when(sessionService)
                .deleteSessionFromGoal(1L, 1L);

        mockMvc.perform(post("/home/goals/1/sessions/delete/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error_page"));

        verify(sessionService, times(1)).deleteSessionFromGoal(1L, 1L);
    }
}