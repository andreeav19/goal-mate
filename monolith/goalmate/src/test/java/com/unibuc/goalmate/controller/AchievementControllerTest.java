package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.advice.ErrorControllerAdvice;
import com.unibuc.goalmate.dto.AchievementRequestDto;
import com.unibuc.goalmate.security.SecurityConfig;
import com.unibuc.goalmate.security.UserDetailsServiceImpl;
import com.unibuc.goalmate.service.AchievementService;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.GoalService;
import jakarta.persistence.EntityNotFoundException;
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

@Import({SecurityConfig.class, ErrorControllerAdvice.class})
@WebMvcTest(AchievementController.class)
class AchievementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private AchievementService achievementService;

    //TODO: getGoalAchievements_Authenticated

    @Test
    void getGoalAchievements_NotAuthenticated_ShouldRedirectLoginPage() throws Exception {
        mockMvc.perform(get("/home/goals/1/achievements"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser
    void getGoalAchievements_Authenticated_ShouldReturnAddAchievementPage() throws Exception {
        when(authService.isCurrentUserAdmin()).thenReturn(true);
        when(goalService.getGoalTargetAmount(1L)).thenReturn(10f);
        when(goalService.getGoalCurrentAmount(1L)).thenReturn(1f);

        mockMvc.perform(get("/home/goals/1/achievements/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("achievement/add_achievement_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("achievementRequest", instanceOf(AchievementRequestDto.class)))
                .andExpect(model().attribute("goalId", 1L))
                .andExpect(model().attribute("goalTarget", 10f))
                .andExpect(model().attribute("goalCurrentAmount", 1f));
    }

    @Test
    void getGoalAchievements_NotAuthenticated_ShouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/home/goals/1/achievements/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser
    void addAchievement_ValidInput_ShouldRedirectAchievementsPage() throws Exception {
        AchievementRequestDto dto = new AchievementRequestDto("Achievement", 10f);

        mockMvc.perform(post("/home/goals/1/achievements/add")
                .flashAttr("achievementRequest", dto)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals/1/achievements"));
    }

    @Test
    @WithMockUser
    void addAchievement_InvalidInput_ShouldRedirectAddAchievementPage() throws Exception {
        AchievementRequestDto dto = new AchievementRequestDto(null, null);

        mockMvc.perform(post("/home/goals/1/achievements/add")
                        .flashAttr("achievementRequest", dto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("achievement/add_achievement_page"))
                .andExpect(model().attributeExists("errors"));

        verify(achievementService, never()).addAchievementToGoal(1L, dto);
    }

    @Test
    @WithMockUser
    void deleteAchievement_ValidInput_ShouldRedirectAchievementsPage() throws Exception {
        mockMvc.perform(post("/home/goals/1/achievements/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/goals/1/achievements"));

        verify(achievementService, times(1)).deleteAchievementFromGoal(1L, 1L);
    }

    @Test
    @WithMockUser
    void deleteAchievement_InvalidInput_ShouldRedirectAchievementsPage() throws Exception {
        doThrow(new EntityNotFoundException("Goal not found."))
                .when(achievementService)
                .deleteAchievementFromGoal(1L, 1L);

        mockMvc.perform(post("/home/goals/1/achievements/delete/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error_page"));

        verify(achievementService, times(1)).deleteAchievementFromGoal(1L, 1L);
    }
}