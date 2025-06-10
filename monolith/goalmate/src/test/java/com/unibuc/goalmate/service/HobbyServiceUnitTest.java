package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.HobbyOptionResponseDto;
import com.unibuc.goalmate.dto.HobbyRequestDto;
import com.unibuc.goalmate.dto.HobbyResponseDto;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HobbyServiceUnitTest {
    private HobbyRepository hobbyRepository;
    private HobbyService hobbyService;

    @BeforeEach
    void setUp() {
        hobbyRepository = mock(HobbyRepository.class);
        hobbyService = new HobbyService(hobbyRepository);
    }

    @Test
    void getAllHobbies_ShouldReturnPagedHobbyResponseDto() {
        Hobby hobby1 = new Hobby();
        hobby1.setName("Cycling");
        hobby1.setDescription("Riding bikes");

        Hobby hobby2 = new Hobby();
        hobby2.setName("Running");
        hobby2.setDescription("Running outdoors");

        List<Hobby> hobbies = List.of(hobby1, hobby2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());
        Page<Hobby> hobbyPage = new PageImpl<>(hobbies, pageable, hobbies.size());

        when(hobbyRepository.findAll(pageable)).thenReturn(hobbyPage);

        Page<HobbyResponseDto> result = hobbyService.getAllHobbies(0, 2, "name", "asc");

        assertEquals(2, result.getContent().size());
        assertEquals("Cycling", result.getContent().get(0).getName());
        assertEquals("Riding bikes", result.getContent().get(0).getDescription());
        assertEquals("Running", result.getContent().get(1).getName());
        assertEquals("Running outdoors", result.getContent().get(1).getDescription());

        verify(hobbyRepository).findAll(pageable);
    }

    @Test
    void addHobby_ShouldSaveHobbyIfNameDoesNotExist() {
        HobbyRequestDto request = new HobbyRequestDto();
        request.setName("Gardening");
        request.setDescription("Plant and grow flowers");

        when(hobbyRepository.existsByName(request.getName())).thenReturn(false);

        hobbyService.addHobby(request);

        verify(hobbyRepository).save(argThat(hobby ->
                hobby.getName().equals("Gardening") &&
                        hobby.getDescription().equals("Plant and grow flowers")
        ));
    }

    @Test
    void addHobby_ShouldThrowExceptionIfNameExists() {
        HobbyRequestDto request = new HobbyRequestDto();
        request.setName("Gardening");

        when(hobbyRepository.existsByName(request.getName())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> hobbyService.addHobby(request));
        verify(hobbyRepository, never()).save(any());
    }

    @Test
    void deleteHobbyByName_ShouldDeleteIfHobbyExists() {
        String hobbyName = "Gardening";

        when(hobbyRepository.existsByName(hobbyName)).thenReturn(true);

        hobbyService.deleteHobbyByName(hobbyName);
        verify(hobbyRepository).deleteByName(hobbyName);
    }

    @Test
    void deleteHobbyByName_ShouldThrowIfHobbyDoesNotExist() {
        String hobbyName = "Gardening";

        when(hobbyRepository.existsByName(hobbyName)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> hobbyService.deleteHobbyByName(hobbyName));
        verify(hobbyRepository, never()).deleteByName(any());
    }

    @Test
    void getHobbyOptions_shouldReturnMappedList() {
        Hobby hobby1 = new Hobby();
        hobby1.setHobbyId(1L);
        hobby1.setName("Gardening");

        Hobby hobby2 = new Hobby();
        hobby2.setHobbyId(2L);
        hobby2.setName("Cooking");

        when(hobbyRepository.findAll()).thenReturn(List.of(hobby1, hobby2));

        List<HobbyOptionResponseDto> result = hobbyService.getHobbyOptions();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getHobbyId());
        assertEquals("Gardening", result.get(0).getHobbyName());
        assertEquals(2L, result.get(1).getHobbyId());
        assertEquals("Cooking", result.get(1).getHobbyName());
    }
}