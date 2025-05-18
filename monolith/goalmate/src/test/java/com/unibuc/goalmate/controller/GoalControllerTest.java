package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.advice.ErrorControllerAdvice;
import com.unibuc.goalmate.dto.*;
import com.unibuc.goalmate.security.SecurityConfig;
import com.unibuc.goalmate.security.UserDetailsServiceImpl;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import com.unibuc.goalmate.service.HobbyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
@WebMvcTest(GoalController.class)
class GoalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private HobbyService hobbyService;

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getGoalSessions_Authenticated_ShouldReturnHomePage() throws Exception {
        List<GoalResponseDto> goals = List.of(new GoalResponseDto(
                1L,
                1L,
                "Drawing",
                "Learning oil painting",
                100f,
                20f,
                "drawings",
                LocalDate.now().plusMonths(3)
        ));

        String email = "user@example.com";
        when(goalService.getGoalsByLoggedUser(email)).thenReturn(goals);
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(get("/home/goals"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/goal_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("goals", goals));
    }

    @Test
    void getGoalSessions_NotAuthenticated_ShouldRedirectLoginPage() throws Exception {
        mockMvc.perform(get("/home/goals"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser
    void getAddGoalPage_Authenticated_ShouldReturnAddGoalPage() throws Exception {
        List<HobbyOptionResponseDto> hobbies = List.of(new HobbyOptionResponseDto(1L, "Drawing"));

        when(hobbyService.getHobbyOptions()).thenReturn(hobbies);
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(get("/home/goals/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/add_goal_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("hobbies", hobbies))
                .andExpect(model().attribute("goalRequest", instanceOf(GoalRequestDto.class)))
                .andExpect(model().attribute("today", LocalDate.now()));
    }

    @Test
    void getAddGoalPage_NotAuthenticated_ShouldRedirectLoginPage() throws Exception {
        mockMvc.perform(get("/home/goals/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void postAddGoal_ValidInput_ShouldRedirectGoalsPage() throws Exception {
        String email = "user@example.com";
        GoalRequestDto dto = new GoalRequestDto(
                1L, null, 10f, "km", LocalDate.now());

        mockMvc.perform(post("/home/goals/add")
                        .flashAttr("goalRequest", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals"));

        verify(goalService, times(1)).addGoalToLoggedUser(dto, email);
    }

    @Test
    @WithMockUser
    void postAddGoal_InvalidInput_ShouldNotRedirectGoalsPage() throws Exception {
        String email = "admin@example.com";
        GoalRequestDto dto = new GoalRequestDto();

        mockMvc.perform(post("/home/goals/add")
                        .flashAttr("goalRequest", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("home/add_goal_page"))
                .andExpect(model().attributeExists("errors"));

        verify(goalService, never()).addGoalToLoggedUser(dto, email);
    }

    @Test
    @WithMockUser
    void getEditGoalPage_Authenticated_ShouldReturnAddGoalPage() throws Exception {
        List<HobbyOptionResponseDto> hobbies = List.of(new HobbyOptionResponseDto(1L, "Drawing"));
        GoalResponseDto goal = new GoalResponseDto(
                1L,
                1L,
                "Running",
                null,
                10f,
                2f,
                "km",
                LocalDate.now()
        );

        when(hobbyService.getHobbyOptions()).thenReturn(hobbies);
        when(authService.isCurrentUserAdmin()).thenReturn(true);
        when(goalService.getGoalById(1L)).thenReturn(goal);

        mockMvc.perform(get("/home/goals/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/edit_goal_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("hobbies", hobbies))
                .andExpect(model().attribute("goalRequest", goal))
                .andExpect(model().attribute("today", LocalDate.now()));
    }

    @Test
    void getEditGoalPage_NotAuthenticated_ShouldRedirectLogin() throws Exception {
        mockMvc.perform(get("/home/goals/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser
    void postEditGoal_ValidInput_ShouldRedirectGoalsPage() throws Exception {
        String email = "admin@example.com";
        GoalRequestDto dto = new GoalRequestDto(
                1L, null, 10f, "km", LocalDate.now());

        mockMvc.perform(post("/home/goals/edit/1")
                        .flashAttr("goalRequest", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals"));

        verify(goalService, times(1)).editGoal(1L, dto);
    }

    @Test
    @WithMockUser
    void postEditGoal_InvalidInput_ShouldNotRedirectGoalsPage() throws Exception {
        GoalRequestDto dto = new GoalRequestDto();
        when(goalService.getGoalById(1L)).thenReturn(Mockito.mock(GoalResponseDto.class));

        mockMvc.perform(post("/home/goals/edit/1")
                        .flashAttr("goalRequest", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("home/edit_goal_page"))
                .andExpect(model().attributeExists("errors"));

        verify(goalService, never()).editGoal(1L, dto);
    }

    @Test
    @WithMockUser
    void postDeleteGoal_ValidInput_ShouldRedirectGoalsPage() throws Exception {
        mockMvc.perform(post("/home/goals/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals"));

        verify(goalService, times(1)).deleteGoal(1L);
    }

    @Test
    @WithMockUser
    void postDeleteGoal_InvalidInput_ShouldRedirectErrorPage() throws Exception {
        doThrow(new EntityNotFoundException("Goal not found."))
                .when(goalService)
                .deleteGoal(1L);

        mockMvc.perform(post("/home/goals/delete/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error_page"));

        verify(goalService, times(1)).deleteGoal(1L);
    }
}