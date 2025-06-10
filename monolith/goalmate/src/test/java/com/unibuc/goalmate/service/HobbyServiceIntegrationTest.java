package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.HobbyOptionResponseDto;
import com.unibuc.goalmate.dto.HobbyRequestDto;
import com.unibuc.goalmate.dto.HobbyResponseDto;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class HobbyServiceIntegrationTest {
    @Autowired
    private HobbyRepository hobbyRepository;

    @Autowired
    private HobbyService hobbyService;

    @BeforeEach
    void setup() {
        Hobby hobby1 = new Hobby();
        hobby1.setName("Basketball");
        hobby1.setDescription("Playing basketball");
        hobbyRepository.save(hobby1);

        Hobby hobby2 = new Hobby();
        hobby2.setName("Chess");
        hobby2.setDescription("Playing chess");
        hobbyRepository.save(hobby2);

        Hobby hobby3 = new Hobby();
        hobby3.setName("Running");
        hobby3.setDescription("Running outdoors");
        hobbyRepository.save(hobby3);
    }

    @Test
    void getAllHobbies_ShouldReturnPagedHobbyDto_SortedAsc() {
        Page<HobbyResponseDto> page = hobbyService.getAllHobbies(0, 2, "name", "asc");

        assertEquals(2, page.getSize());
        assertEquals(0, page.getNumber());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());

        assertEquals("Basketball", page.getContent().get(0).getName());
        assertEquals("Chess", page.getContent().get(1).getName());
    }

    @Test
    void getAllHobbies_ShouldReturnPagedHobbyDto_SortedDesc() {
        Page<HobbyResponseDto> page = hobbyService.getAllHobbies(0, 2, "name", "desc");

        assertEquals(2, page.getSize());
        assertEquals(0, page.getNumber());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());

        assertEquals("Running", page.getContent().get(0).getName());
        assertEquals("Chess", page.getContent().get(1).getName());
    }

    @Test
    void addHobby_ShouldSaveNewHobby_WhenNameIsUnique() {
        HobbyRequestDto request = new HobbyRequestDto();
        request.setName("Painting");
        request.setDescription("Creative painting hobby");

        hobbyService.addHobby(request);

        boolean exists = hobbyRepository.existsByName("Painting");
        assertTrue(exists);

        Hobby savedHobby = hobbyRepository.findByName("Painting").orElseThrow();
        assertEquals("Creative painting hobby", savedHobby.getDescription());
    }

    @Test
    void addHobby_ShouldThrowException_WhenNameAlreadyExists() {
        HobbyRequestDto request = new HobbyRequestDto();
        request.setName("Basketball");
        request.setDescription("Some description");

        assertThrows(RuntimeException.class, () -> hobbyService.addHobby(request));
    }

    @Test
    void deleteHobbyByName_ShouldDeleteHobby_WhenHobbyExists() {
        assertTrue(hobbyRepository.existsByName("Basketball"));
        hobbyService.deleteHobbyByName("Basketball");
        assertFalse(hobbyRepository.existsByName("Basketball"));
    }

    @Test
    void deleteHobbyByName_ShouldThrowException_WhenHobbyDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> hobbyService.deleteHobbyByName("NonExistentHobby"));
    }

    @Test
    void getHobbyOptions_ShouldReturnAllHobbyOptions() {
        List<HobbyOptionResponseDto> options = hobbyService.getHobbyOptions();

        assertEquals(3, options.size());

        List<String> names = options.stream().map(HobbyOptionResponseDto::getHobbyName).toList();
        assertTrue(names.contains("Basketball"));
        assertTrue(names.contains("Chess"));
        assertTrue(names.contains("Running"));
    }
}