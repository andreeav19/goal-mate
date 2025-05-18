package com.unibuc.goalmate.controller;

import com.unibuc.goalmate.dto.HobbyRequestDto;
import com.unibuc.goalmate.dto.HobbyResponseDto;
import com.unibuc.goalmate.service.AuthService;
import com.unibuc.goalmate.service.HobbyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(HobbyController.class)
class HobbyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private HobbyService hobbyService;

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void getAllHobbies_AsAdmin_ShouldReturnHobbiesPage() throws Exception {
        List<HobbyResponseDto> hobbies = List.of(
                new HobbyResponseDto("Drawing", "")
        );

        when(authService.isCurrentUserAdmin()).thenReturn(true);
        when(hobbyService.getAllHobbies()).thenReturn(hobbies);

        mockMvc.perform(get("/hobbies"))
                .andExpect(status().isOk())
                .andExpect(view().name("hobby/hobbies"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("hobbies", hobbies))
                .andExpect(model().attributeExists("hobbyRequest"));
    }

    @Test
    void getAllHobbies_NotAuthenticated_ShouldNotReturnHobbiesPage() throws Exception {
        mockMvc.perform(get("/hobbies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void getAddHobbyPage_AsAdmin_ShouldReturnAddHobbyPage() throws Exception {
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(get("/hobbies/add-hobby"))
                .andExpect(status().isOk())
                .andExpect(view().name("hobby/add_hobby_page"))
                .andExpect(model().attribute("isAdmin", true))
                .andExpect(model().attribute("hobbyRequest", instanceOf(HobbyRequestDto.class)));
    }

    @Test
    void getAddHobbyPage_NotAuthenticated_ShouldNotReturnAddHobbyPage() throws Exception {
        mockMvc.perform(get("/hobbies/add-hobby"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void postAddHobby_ValidInput_ShouldCallServiceAndRedirect() throws Exception {
        HobbyRequestDto dto = new HobbyRequestDto("Drawing", "");

        mockMvc.perform(post("/hobbies/add-hobby")
                        .flashAttr("hobbyRequest", dto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/hobbies"));

        verify(hobbyService, times(1)).addHobby(dto);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void postAddHobby_InvalidInput_ShouldReturnToFormWithErrors() throws Exception {
        HobbyRequestDto invalidDto = new HobbyRequestDto("", "");

        mockMvc.perform(post("/hobbies/add-hobby")
                        .flashAttr("hobbyRequest", invalidDto)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("hobby/add_hobby_page"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attributeExists("isAdmin"))
                .andExpect(model().attributeExists("hobbyRequest"));

        verify(hobbyService, never()).addHobby(any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
    void postDeleteHobby_ValidInput_ShouldCallServiceAndRedirect() throws Exception {
        String hobbyName = "Drawing";

        mockMvc.perform(post("/hobbies/delete-hobby")
                        .param("hobbyName", hobbyName)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/hobbies"));

        verify(hobbyService, times(1)).deleteHobbyByName(hobbyName);
    }
}