package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.GoalAchievementsResponseDto;
import com.unibuc.goalmate.dto.GoalRequestDto;
import com.unibuc.goalmate.dto.GoalResponseDto;
import com.unibuc.goalmate.dto.GoalSessionsResponseDto;
import com.unibuc.goalmate.model.*;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.HobbyRepository;
import com.unibuc.goalmate.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class GoalServiceIntegrationTest {
    @Autowired
    GoalRepository goalRepository;

    @Autowired
    GoalMateUserRepository userRepository;

    @Autowired
    HobbyRepository hobbyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    GoalService goalService;

    private GoalMateUser testUser;
    private Role userRole;

    @BeforeEach
    void setup() {
        userRole = roleRepository.findByRoleName(RoleName.USER).orElseThrow();

        testUser = new GoalMateUser();
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setPassword("test");
        testUser.setRoles(new HashSet<>(Set.of(userRole)));

        roleRepository.save(userRole);
        userRepository.save(testUser);
    }

//    @Test
//    void getGoalsByLoggedUser_ShouldReturnPagedGoalsForUser() {
//        Hobby hobby = new Hobby();
//        hobby.setName("Reading");
//        hobbyRepository.save(hobby);
//
//        for (int i = 1; i <= 15; i++) {
//            Goal goal = new Goal();
//            goal.setUser(testUser);
//            goal.setHobby(hobby);
//            goal.setDescription("Goal " + i);
//            goal.setTargetAmount(100f + i);
//            goal.setCurrentAmount((float) i);
//            goal.setTargetUnit("pages");
//            goal.setDeadline(LocalDate.now().plusDays(10 + i));
//            goalRepository.save(goal);
//        }
//
//        Pageable pageable = PageRequest.of(0, 10);
//
//        Page<GoalResponseDto> page = goalService.getGoalsByLoggedUser(testUser.getEmail(), pageable);
//
//        assertEquals(10, page.getSize());
//        assertEquals(0, page.getNumber());
//        assertEquals(15, page.getTotalElements());
//        assertEquals(2, page.getTotalPages());
//        assertEquals(10, page.getContent().size());
//
//        GoalResponseDto firstGoal = page.getContent().getFirst();
//        assertNotNull(firstGoal.getGoalId());
//        assertEquals(hobby.getHobbyId(), firstGoal.getHobbyId());
//        assertEquals("Reading", firstGoal.getHobbyName());
//        assertTrue(firstGoal.getDescription().startsWith("Goal"));
//    }
//
//    @Test
//    void addGoalToLoggedUser_ShouldSaveGoalAndAssociateWithUserAndHobby() {
//        Hobby hobby = new Hobby();
//        hobby.setName("Running");
//        hobbyRepository.save(hobby);
//
//        GoalRequestDto goalRequest = new GoalRequestDto();
//        goalRequest.setHobbyId(hobby.getHobbyId());
//        goalRequest.setDescription("Run 5km every day");
//        goalRequest.setTargetAmount(30f);
//        goalRequest.setUnit("days");
//        goalRequest.setDeadline(LocalDate.now().plusDays(40));
//
//        goalService.addGoalToLoggedUser(goalRequest, testUser.getEmail());
//
//        List<Goal> userGoals = goalRepository.findByUser_Email(testUser.getEmail(), Pageable.unpaged()).getContent();
//        assertFalse(userGoals.isEmpty());
//
//        Goal savedGoal = userGoals.getFirst();
//        assertEquals("Run 5km every day", savedGoal.getDescription());
//        assertEquals(30f, savedGoal.getTargetAmount());
//        assertEquals("days", savedGoal.getTargetUnit());
//        assertEquals(hobby.getHobbyId(), savedGoal.getHobby().getHobbyId());
//        assertEquals(testUser.getUserId(), savedGoal.getUser().getUserId());
//
//        Hobby savedHobby = hobbyRepository.findById(hobby.getHobbyId()).orElseThrow();
//        assertTrue(savedHobby.getGoals().contains(savedGoal));
//
//        GoalMateUser savedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
//        assertTrue(savedUser.getGoals().contains(savedGoal));
//    }

    @Test
    void addGoalToLoggedUser_ShouldThrow_WhenUserNotFound() {
        GoalRequestDto goalRequest = new GoalRequestDto();
        assertThrows(EntityNotFoundException.class, () ->
                goalService.addGoalToLoggedUser(goalRequest, "nonexistent@email.com"));
    }

    @Test
    void addGoalToLoggedUser_ShouldThrow_WhenHobbyNotFound() {
        GoalRequestDto goalRequest = new GoalRequestDto();
        goalRequest.setHobbyId(999L);

        assertThrows(EntityNotFoundException.class, () ->
                goalService.addGoalToLoggedUser(goalRequest, testUser.getEmail()));
    }

    @Test
    void getGoalById_ShouldReturnGoalResponseDto_WhenGoalExists() {
        Hobby hobby = new Hobby();
        hobby.setName("Cycling");
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("Cycle 100 miles");
        goal.setTargetAmount(100f);
        goal.setCurrentAmount(20f);
        goal.setTargetUnit("miles");
        goal.setDeadline(LocalDate.now().plusDays(30));
        goalRepository.save(goal);

        GoalResponseDto dto = goalService.getGoalById(goal.getGoalId());

        assertEquals(goal.getGoalId(), dto.getGoalId());
        assertEquals(hobby.getHobbyId(), dto.getHobbyId());
        assertEquals("Cycling", dto.getHobbyName());
        assertEquals("Cycle 100 miles", dto.getDescription());
        assertEquals(100, dto.getTargetAmount());
        assertEquals(20, dto.getCurrentAmount());
        assertEquals("miles", dto.getUnit());
        assertEquals(goal.getDeadline(), dto.getDeadline());
    }

    @Test
    void getGoalById_ShouldThrowEntityNotFoundException_WhenGoalDoesNotExist() {
        Long nonExistentId = 9999L;
        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalById(nonExistentId));
    }

    @Test
    void editGoal_ShouldUpdateGoalDetailsAndHobby_WhenDataIsValid() {
        Hobby oldHobby = new Hobby();
        oldHobby.setName("Old Hobby");
        hobbyRepository.save(oldHobby);

        Hobby newHobby = new Hobby();
        newHobby.setName("New Hobby");
        hobbyRepository.save(newHobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(oldHobby);
        goal.setDescription("Old Description");
        goal.setTargetAmount(50f);
        goal.setTargetUnit("units");
        goal.setDeadline(LocalDate.now().plusDays(10));
        goalRepository.save(goal);

        GoalRequestDto updateDto = new GoalRequestDto();
        updateDto.setDescription("New Description");
        updateDto.setTargetAmount(100f);
        updateDto.setUnit("new units");
        updateDto.setDeadline(LocalDate.now().plusDays(20));
        updateDto.setHobbyId(newHobby.getHobbyId());

        goalService.editGoal(goal.getGoalId(), updateDto);

        Goal updatedGoal = goalRepository.findById(goal.getGoalId()).orElseThrow();

        assertEquals("New Description", updatedGoal.getDescription());
        assertEquals(100, updatedGoal.getTargetAmount());
        assertEquals("new units", updatedGoal.getTargetUnit());
        assertEquals(updateDto.getDeadline(), updatedGoal.getDeadline());
        assertEquals(newHobby.getHobbyId(), updatedGoal.getHobby().getHobbyId());

        Hobby refreshedOldHobby = hobbyRepository.findById(oldHobby.getHobbyId()).orElseThrow();
        assertFalse(refreshedOldHobby.getGoals().contains(updatedGoal));

        Hobby refreshedNewHobby = hobbyRepository.findById(newHobby.getHobbyId()).orElseThrow();
        assertTrue(refreshedNewHobby.getGoals().contains(updatedGoal));
    }

    @Test
    void editGoal_ShouldThrow_WhenGoalNotFound() {
        GoalRequestDto dto = new GoalRequestDto();
        dto.setHobbyId(1L);
        assertThrows(EntityNotFoundException.class, () -> goalService.editGoal(9999L, dto));
    }

    @Test
    void editGoal_ShouldThrow_WhenHobbyNotFound() {
        Hobby hobby = new Hobby();
        hobby.setName("Existing Hobby");
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("desc");
        goal.setTargetAmount(10f);
        goal.setTargetUnit("unit");
        goal.setDeadline(LocalDate.now());
        goalRepository.save(goal);

        GoalRequestDto dto = new GoalRequestDto();
        dto.setHobbyId(9999L);
        dto.setDescription("desc");
        dto.setTargetAmount(1f);
        dto.setUnit("unit");
        dto.setDeadline(LocalDate.now());

        assertThrows(EntityNotFoundException.class, () -> goalService.editGoal(goal.getGoalId(), dto));
    }

    @Test
    void deleteGoal_ShouldRemoveGoalAndUpdateHobby() {
        Hobby hobby = new Hobby();
        hobby.setName("Hobby to delete goal from");
        hobby.setGoals(new ArrayList<>());
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("Goal to delete");
        goal.setTargetAmount(10f);
        goal.setTargetUnit("units");
        goal.setDeadline(LocalDate.now().plusDays(10));
        goalRepository.save(goal);

        hobby.getGoals().add(goal);
        hobbyRepository.save(hobby);

        goalService.deleteGoal(goal.getGoalId());

        assertFalse(goalRepository.findById(goal.getGoalId()).isPresent());

        Hobby updatedHobby = hobbyRepository.findById(hobby.getHobbyId()).orElseThrow();
        assertFalse(updatedHobby.getGoals().contains(goal));
    }

    @Test
    void deleteGoal_ShouldThrowEntityNotFoundException_WhenGoalNotFound() {
        assertThrows(EntityNotFoundException.class, () -> goalService.deleteGoal(9999L));
    }

//    @Test
//    void getGoalSessions_ShouldReturnCorrectDto_WhenGoalExists() {
//        Hobby hobby = new Hobby();
//        hobby.setName("Test Hobby");
//        hobby.setGoals(new ArrayList<>());
//        hobbyRepository.save(hobby);
//
//        Goal goal = new Goal();
//        goal.setUser(testUser);
//        goal.setHobby(hobby);
//        goal.setDescription("Goal with sessions");
//        goal.setTargetAmount(100f);
//        goal.setCurrentAmount(20f);
//        goal.setTargetUnit("units");
//        goal.setDeadline(LocalDate.now().plusDays(15));
//        goal.setSessions(new ArrayList<>());
//        goalRepository.save(goal);
//
//        Session session1 = new Session();
//        session1.setDate(LocalDate.now());
//        session1.setProgressAmount(10f);
//        session1.setGoal(goal);
//
//        Session session2 = new Session();
//        session2.setDate(LocalDate.now().plusDays(1));
//        session2.setProgressAmount(15f);
//        session2.setGoal(goal);
//
//        goal.getSessions().add(session1);
//        goal.getSessions().add(session2);
//
//        goalRepository.save(goal);
//
//        GoalSessionsResponseDto dto = goalService.getGoalSessions(goal.getGoalId());
//
//        assertEquals(goal.getGoalId(), dto.getGoalId());
//        assertEquals(hobby.getName(), dto.getGoalName());
//        assertEquals(goal.getTargetUnit(), dto.getUnit());
//        assertEquals(goal.getTargetAmount(), dto.getTargetAmount());
//        assertEquals(goal.getCurrentAmount(), dto.getCurrentAmount());
//
//        assertEquals(2, dto.getSessions().size());
//
//        assertTrue(dto.getSessions().stream()
//                .anyMatch(s -> s.getProgressAmount() == 10 && s.getDate().equals(session1.getDate())));
//
//        assertTrue(dto.getSessions().stream()
//                .anyMatch(s -> s.getProgressAmount() == 15 && s.getDate().equals(session2.getDate())));
//    }

//    @Test
//    void getGoalSessions_ShouldThrow_WhenGoalNotFound() {
//        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalSessions(9999L));
//    }

    @Test
    void getGoalAchievements_ShouldReturnCorrectDto_WhenGoalExists() {
        Hobby hobby = new Hobby();
        hobby.setName("Achievement Hobby");
        hobby.setGoals(new ArrayList<>());
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("Goal with achievements");
        goal.setTargetAmount(200f);
        goal.setCurrentAmount(50f);
        goal.setTargetUnit("points");
        goal.setDeadline(LocalDate.now().plusDays(30));
        goal.setAchievements(new ArrayList<>());
        goalRepository.save(goal);

        Achievement achievement1 = new Achievement();
        achievement1.setTitle("First Milestone");
        achievement1.setAmountToReach(10f);
        achievement1.setDateAwarded(LocalDate.now().minusDays(1));
        achievement1.setGoal(goal);

        Achievement achievement2 = new Achievement();
        achievement2.setTitle("Halfway There");
        achievement2.setAmountToReach(100f);
        achievement2.setDateAwarded(LocalDate.now().minusDays(5));
        achievement2.setGoal(goal);

        goal.getAchievements().add(achievement1);
        goal.getAchievements().add(achievement2);

        goalRepository.save(goal);

        GoalAchievementsResponseDto dto = goalService.getGoalAchievements(goal.getGoalId());

        assertEquals(goal.getGoalId(), dto.getGoalId());
        assertEquals(hobby.getName(), dto.getGoalName());
        assertEquals(goal.getTargetUnit(), dto.getUnit());
        assertEquals(goal.getTargetAmount(), dto.getTargetAmount());
        assertEquals(goal.getCurrentAmount(), dto.getCurrentAmount());

        assertEquals(2, dto.getAchievements().size());

        assertTrue(dto.getAchievements().stream()
                .anyMatch(a -> a.getTitle().equals("First Milestone") && a.getAmountToReach() == 10));

        assertTrue(dto.getAchievements().stream()
                .anyMatch(a -> a.getTitle().equals("Halfway There") && a.getAmountToReach() == 100));
    }

    @Test
    void getGoalAchievements_ShouldThrow_WhenGoalNotFound() {
        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalAchievements(9999L));
    }

    @Test
    void getGoalDeadline_ShouldReturnDeadline_WhenGoalExists() {
        Hobby hobby = new Hobby();
        hobby.setName("Deadline Hobby");
        hobbyRepository.save(hobby);

        LocalDate deadline = LocalDate.now().plusDays(20);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("Goal with deadline");
        goal.setTargetAmount(50f);
        goal.setCurrentAmount(10f);
        goal.setTargetUnit("units");
        goal.setDeadline(deadline);
        goalRepository.save(goal);

        LocalDate returnedDeadline = goalService.getGoalDeadline(goal.getGoalId());

        assertEquals(deadline, returnedDeadline);
    }

    @Test
    void getGoalDeadline_ShouldThrow_WhenGoalNotFound() {
        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalDeadline(9999L));
    }

    @Test
    void getGoalTargetAmount_ShouldReturnTargetAmount_WhenGoalExists() {
        Hobby hobby = new Hobby();
        hobby.setName("Target Amount Hobby");
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("Goal with target amount");
        goal.setTargetAmount(150f);
        goal.setCurrentAmount(75f);
        goal.setTargetUnit("units");
        goal.setDeadline(LocalDate.now().plusDays(10));
        goalRepository.save(goal);

        Float targetAmount = goalService.getGoalTargetAmount(goal.getGoalId());

        assertEquals(150f, targetAmount);
    }

    @Test
    void getGoalTargetAmount_ShouldThrow_WhenGoalNotFound() {
        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalTargetAmount(9999L));
    }

    @Test
    void getGoalCurrentAmount_ShouldReturnCurrentAmount_WhenGoalExists() {
        Hobby hobby = new Hobby();
        hobby.setName("Current Amount Hobby");
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setUser(testUser);
        goal.setHobby(hobby);
        goal.setDescription("Goal with current amount");
        goal.setTargetAmount(200f);
        goal.setCurrentAmount(120f);
        goal.setTargetUnit("units");
        goal.setDeadline(LocalDate.now().plusDays(20));
        goalRepository.save(goal);

        Float currentAmount = goalService.getGoalCurrentAmount(goal.getGoalId());

        assertEquals(120f, currentAmount);
    }

    @Test
    void getGoalCurrentAmount_ShouldThrow_WhenGoalNotFound() {
        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalCurrentAmount(9999L));
    }

}