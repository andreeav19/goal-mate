package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.GoalRequestDto;
import com.unibuc.goalmate.dto.GoalResponseDto;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoalServiceUnitTest {
    private GoalService goalService;
    private GoalRepository goalRepository;
    private GoalMateUserRepository userRepository;
    private HobbyRepository hobbyRepository;

    @BeforeEach
    void setUp() {
        goalRepository = mock(GoalRepository.class);
        userRepository = mock(GoalMateUserRepository.class);
        hobbyRepository = mock(HobbyRepository.class);
        goalService = new GoalService(goalRepository, userRepository, hobbyRepository);
    }

    @Test
    void getGoalsByLoggedUser_ShouldReturnMappedGoals() {
        String userEmail = "user@example.com";

        Hobby hobby = new Hobby();
        hobby.setHobbyId(1L);
        hobby.setName("Painting");

        Goal goal = new Goal();
        goal.setGoalId(10L);
        goal.setHobby(hobby);
        goal.setDescription("Finish a landscape");
        goal.setTargetAmount(100f);
        goal.setCurrentAmount(30f);
        goal.setTargetUnit("hours");
        goal.setDeadline(LocalDate.of(2025, 12, 31));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Goal> page = new PageImpl<>(List.of(goal));

        when(goalRepository.findByUser_Email(userEmail, pageable)).thenReturn(page);
        Page<GoalResponseDto> result = goalService.getGoalsByLoggedUser(userEmail, pageable);

        assertEquals(1, result.getTotalElements());

        GoalResponseDto dto = result.getContent().getFirst();
        assertEquals(10L, dto.getGoalId());
        assertEquals(1L, dto.getHobbyId());
        assertEquals("Painting", dto.getHobbyName());
        assertEquals("Finish a landscape", dto.getDescription());
        assertEquals(100, dto.getTargetAmount());
        assertEquals(30, dto.getCurrentAmount());
        assertEquals("hours", dto.getUnit());
        assertEquals(LocalDate.of(2025, 12, 31), dto.getDeadline());
    }

    @Test
    void addGoalToLoggedUser_ShouldThrowIfUserNotFound() {
        String email = "missing@example.com";
        GoalRequestDto dto = new GoalRequestDto();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.addGoalToLoggedUser(dto, email));
        verifyNoInteractions(hobbyRepository, goalRepository);
    }

    @Test
    void addGoalToLoggedUser_ShouldThrowIfHobbyNotFound() {
        String email = "user@example.com";
        GoalRequestDto dto = new GoalRequestDto();
        dto.setHobbyId(42L);

        GoalMateUser user = new GoalMateUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hobbyRepository.findById(dto.getHobbyId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.addGoalToLoggedUser(dto, email));
        verify(goalRepository, never()).save(any());
    }

    @Test
    void addGoalToLoggedUser_ShouldSaveGoalAndAddToLists() {
        String email = "user@example.com";
        GoalRequestDto dto = new GoalRequestDto();
        dto.setDescription("Learn guitar");
        dto.setTargetAmount(50f);
        dto.setUnit("hours");
        dto.setDeadline(LocalDate.of(2025, 1, 1));
        dto.setHobbyId(5L);

        GoalMateUser user = new GoalMateUser();
        user.setGoals(null);

        Hobby hobby = new Hobby();
        hobby.setHobbyId(5L);
        hobby.setGoals(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hobbyRepository.findById(dto.getHobbyId())).thenReturn(Optional.of(hobby));

        goalService.addGoalToLoggedUser(dto, email);

        verify(goalRepository).save(argThat(goal ->
                goal.getDescription().equals(dto.getDescription()) &&
                        Objects.equals(goal.getTargetAmount(), dto.getTargetAmount()) &&
                        goal.getTargetUnit().equals(dto.getUnit()) &&
                        goal.getDeadline().equals(dto.getDeadline()) &&
                        goal.getHobby() == hobby &&
                        goal.getUser() == user
        ));

        assertNotNull(user.getGoals());
        assertNotNull(hobby.getGoals());
        assertTrue(user.getGoals().stream().anyMatch(g -> g.getDescription().equals("Learn guitar")));
        assertTrue(hobby.getGoals().stream().anyMatch(g -> g.getDescription().equals("Learn guitar")));
    }

    @Test
    void getGoalById_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalById(goalId));
    }

    @Test
    void getGoalById_ShouldReturnGoalResponseDto() {
        Long goalId = 1L;

        Hobby hobby = new Hobby();
        hobby.setHobbyId(2L);
        hobby.setName("Chess");

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setHobby(hobby);
        goal.setDescription("Win 10 games");
        goal.setTargetAmount(10f);
        goal.setCurrentAmount(4f);
        goal.setTargetUnit("games");
        goal.setDeadline(LocalDate.of(2025, 5, 20));

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        GoalResponseDto dto = goalService.getGoalById(goalId);

        assertEquals(goalId, dto.getGoalId());
        assertEquals(hobby.getHobbyId(), dto.getHobbyId());
        assertEquals(hobby.getName(), dto.getHobbyName());
        assertEquals("Win 10 games", dto.getDescription());
        assertEquals(10, dto.getTargetAmount());
        assertEquals(4, dto.getCurrentAmount());
        assertEquals("games", dto.getUnit());
        assertEquals(LocalDate.of(2025, 5, 20), dto.getDeadline());
    }

    @Test
    void editGoal_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        GoalRequestDto dto = new GoalRequestDto();

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.editGoal(goalId, dto));
        verifyNoInteractions(hobbyRepository);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void editGoal_ShouldThrowIfHobbyNotFound() {
        Long goalId = 1L;
        GoalRequestDto dto = new GoalRequestDto();
        dto.setHobbyId(42L);

        Goal goal = new Goal();
        Hobby oldHobby = new Hobby();
        goal.setHobby(oldHobby);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(hobbyRepository.findById(dto.getHobbyId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.editGoal(goalId, dto));
        verify(goalRepository, never()).save(any());
    }

    @Test
    void editGoal_ShouldEditWithoutChangingHobby() {
        Long goalId = 1L;
        GoalRequestDto dto = new GoalRequestDto();
        dto.setDescription("New description");
        dto.setTargetAmount(100f);
        dto.setUnit("pages");
        dto.setDeadline(LocalDate.of(2025, 10, 10));
        dto.setHobbyId(10L);

        Hobby hobby = new Hobby();
        hobby.setHobbyId(10L);
        hobby.setGoals(new ArrayList<>());

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setDescription("Old description");
        goal.setTargetAmount(50f);
        goal.setTargetUnit("hours");
        goal.setDeadline(LocalDate.of(2024, 5, 5));
        goal.setHobby(hobby);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(hobbyRepository.findById(dto.getHobbyId())).thenReturn(Optional.of(hobby));

        goalService.editGoal(goalId, dto);

        assertEquals("New description", goal.getDescription());
        assertEquals(100, goal.getTargetAmount());
        assertEquals("pages", goal.getTargetUnit());
        assertEquals(LocalDate.of(2025, 10, 10), goal.getDeadline());
        assertSame(hobby, goal.getHobby());

        verify(goalRepository).save(goal);
    }

    @Test
    void editGoal_ShouldEditAndChangeHobby() {
        Long goalId = 1L;
        GoalRequestDto dto = new GoalRequestDto();
        dto.setDescription("Updated");
        dto.setTargetAmount(200f);
        dto.setUnit("items");
        dto.setDeadline(LocalDate.of(2026, 1, 1));
        dto.setHobbyId(20L);

        Hobby oldHobby = new Hobby();
        oldHobby.setHobbyId(10L);
        oldHobby.setGoals(new ArrayList<>());

        Hobby newHobby = new Hobby();
        newHobby.setHobbyId(20L);
        newHobby.setGoals(new ArrayList<>());

        Goal goal = new Goal();
        goal.setGoalId(goalId);
        goal.setDescription("Old");
        goal.setTargetAmount(100f);
        goal.setTargetUnit("hours");
        goal.setDeadline(LocalDate.of(2024, 5, 5));
        goal.setHobby(oldHobby);

        oldHobby.getGoals().add(goal);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(hobbyRepository.findById(dto.getHobbyId())).thenReturn(Optional.of(newHobby));

        goalService.editGoal(goalId, dto);

        assertEquals("Updated", goal.getDescription());
        assertEquals(200, goal.getTargetAmount());
        assertEquals("items", goal.getTargetUnit());
        assertEquals(LocalDate.of(2026, 1, 1), goal.getDeadline());

        assertSame(newHobby, goal.getHobby());
        assertFalse(oldHobby.getGoals().contains(goal));
        assertTrue(newHobby.getGoals().contains(goal));

        verify(goalRepository).save(goal);
    }

    @Test
    void deleteGoal_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.deleteGoal(goalId));

        verifyNoInteractions(hobbyRepository);
        verify(goalRepository, never()).delete(any());
    }

    @Test
    void deleteGoal_ShouldRemoveGoalFromHobbyAndDeleteGoal() {
        Long goalId = 1L;
        Goal goal = new Goal();
        Hobby hobby = new Hobby();
        hobby.setGoals(new ArrayList<>());
        hobby.getGoals().add(goal);

        goal.setHobby(hobby);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        goalService.deleteGoal(goalId);

        assertFalse(hobby.getGoals().contains(goal));
        verify(hobbyRepository).save(hobby);
        verify(goalRepository).delete(goal);
    }

    //TODO: Test getGoalSessions()

    @Test
    void getGoalDeadline_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalDeadline(goalId));
    }

    @Test
    void getGoalDeadline_ShouldReturnDeadline() {
        Long goalId = 1L;
        LocalDate deadline = LocalDate.of(2025, 12, 31);

        Goal goal = new Goal();
        goal.setDeadline(deadline);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        LocalDate result = goalService.getGoalDeadline(goalId);

        assertEquals(deadline, result);
    }

    @Test
    void getGoalTargetAmount_ShouldThrowIfGoalNotFound() {
        Long goalId = 1L;

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalTargetAmount(goalId));
    }

    @Test
    void getGoalTargetAmount_ShouldReturnTargetAmount() {
        Long goalId = 1L;
        Float targetAmount = 150.5f;

        Goal goal = new Goal();
        goal.setTargetAmount(targetAmount);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        Float result = goalService.getGoalTargetAmount(goalId);

        assertEquals(targetAmount, result);
    }
}