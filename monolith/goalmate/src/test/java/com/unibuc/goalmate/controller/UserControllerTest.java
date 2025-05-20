package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.advice.ErrorControllerAdvice;
import com.unibuc.goalmate.dto.UserResponseDto;
import com.unibuc.goalmate.dto.UserRoleRequestDto;
import com.unibuc.goalmate.security.SecurityConfig;
import com.unibuc.goalmate.security.UserDetailsServiceImpl;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

//    @Test
//    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
//    void getAllUsers_AsAdmin_ShouldReturnUsersPage() throws Exception {
//        List<UserResponseDto> users = List.of(
//                new UserResponseDto(1L,
//                        "admin",
//                        "admin@example.com",
//                        List.of("ADMIN", "USER"),
//                        List.of(),
//                        false)
//        );
//
//        when(userService.getAllUsers("admin@example.com")).thenReturn(users);
//        when(authService.isCurrentUserAdmin()).thenReturn(true);
//
//        mockMvc.perform(get("/admin"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/users"))
//                .andExpect(model().attribute("isAdmin", true))
//                .andExpect(model().attribute("users", users))
//                .andExpect(model().attributeExists("userRoleRequest"));
//    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getAllUsers_AsUser_ShouldRedirectAccessDeniedPage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/auth/access-denied"));
    }

    @Test
    void getAllUsers_NotAuthenticated_ShouldRedirectLoginPage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void addRole_ValidInput_ShouldCallServiceAndRedirect() throws Exception {
        UserRoleRequestDto dto = new UserRoleRequestDto(2L, "ADMIN");

        mockMvc.perform(post("/admin/add-role")
                        .flashAttr("userRoleRequest", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, times(1)).addUserRole(dto);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void addRole_InvalidInput_ShouldNotCallServiceAndRedirect() throws Exception {
        UserRoleRequestDto invalidDto = new UserRoleRequestDto(null, "");

        mockMvc.perform(post("/admin/add-role")
                        .flashAttr("userRoleRequest", invalidDto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, never()).addUserRole(any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void deleteRole_ValidInput_ShouldCallServiceAndRedirect() throws Exception {
        UserRoleRequestDto dto = new UserRoleRequestDto(2L, "ADMIN");

        mockMvc.perform(post("/admin/delete-role")
                        .flashAttr("userRoleRequest", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, times(1)).deleteUserRole(dto);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void deleteRole_InvalidInput_ShouldNotCallServiceAndRedirect() throws Exception {
        UserRoleRequestDto invalidDto = new UserRoleRequestDto(null, "");

        mockMvc.perform(post("/admin/delete-role")
                        .flashAttr("userRoleRequest", invalidDto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, never()).deleteUserRole(any());
    }
}