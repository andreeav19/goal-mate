package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.AchievementRequestDto;
import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Achievement;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.AchievementRepository;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class AchievementServiceIntegrationTest {
    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private HobbyRepository hobbyRepository;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private GoalMateUserRepository userRepository;

    private Goal testGoal;

    @BeforeEach
    void setup() {
        GoalMateUser user = new GoalMateUser();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        Hobby hobby = new Hobby();
        hobby.setName("Test Hobby");
        hobby.setDescription("Hobby description");
        hobbyRepository.save(hobby);

        Goal goal = new Goal();
        goal.setDescription("Another goal");
        goal.setCurrentAmount(0f);
        goal.setTargetAmount(100f);
        goal.setTargetUnit("units");
        goal.setDeadline(LocalDate.now().plusDays(30));
        goal.setHobby(hobby);
        goal.setUser(user);
        goal.setAchievements(new ArrayList<>());
        testGoal = goalRepository.save(goal);
    }

    @Test
    void addAchievementToGoal_ShouldSaveAchievement() {
        AchievementRequestDto dto = new AchievementRequestDto();
        dto.setTitle("First Achievement");
        dto.setAmountToReach(30f);

        achievementService.addAchievementToGoal(testGoal.getGoalId(), dto);

        Goal updatedGoal = goalRepository.findById(testGoal.getGoalId()).orElseThrow();
        List<Achievement> achievements = updatedGoal.getAchievements();

        assertFalse(achievements.isEmpty());
        assertEquals("First Achievement", achievements.getFirst().getTitle());
        assertEquals(30f, achievements.getFirst().getAmountToReach());
    }

    @Test
    void deleteAchievementFromGoal_ShouldRemoveAchievement() {
        Achievement achievement = new Achievement();
        achievement.setTitle("To Delete");
        achievement.setAmountToReach(20f);
        achievement.setGoal(testGoal);
        achievementRepository.save(achievement);

        testGoal.getAchievements().add(achievement);
        goalRepository.save(testGoal);

        achievementService.deleteAchievementFromGoal(testGoal.getGoalId(), achievement.getAchievementId());

        Goal updatedGoal = goalRepository.findById(testGoal.getGoalId()).orElseThrow();
        assertFalse(updatedGoal.getAchievements().contains(achievement));
        assertFalse(achievementRepository.findById(achievement.getAchievementId()).isPresent());
    }

    @Test
    void deleteAchievementFromGoal_ShouldThrowIfAchievementDoesNotBelongToGoal() {
        Goal anotherGoal = new Goal();
        anotherGoal.setDescription("Another goal");
        anotherGoal.setCurrentAmount(0f);
        anotherGoal.setTargetAmount(50f);
        anotherGoal.setTargetUnit("units");
        anotherGoal.setDeadline(LocalDate.now().plusDays(20));
        anotherGoal.setHobby(testGoal.getHobby());
        anotherGoal.setUser(testGoal.getUser());
        anotherGoal = goalRepository.save(anotherGoal);

        Achievement achievement = new Achievement();
        achievement.setTitle("Mismatch");
        achievement.setAmountToReach(10f);
        achievement.setGoal(anotherGoal);
        achievement = achievementRepository.save(achievement);

        Long testGoalId = testGoal.getGoalId();
        Long achievementId = achievement.getAchievementId();

        assertThrows(IllegalArgumentException.class,
                () -> achievementService.deleteAchievementFromGoal(testGoalId, achievementId));
    }

    @Test
    void checkAchievements_ShouldAwardAchievementsWhenCriteriaMet() {
        testGoal.setCurrentAmount(50f);
        goalRepository.save(testGoal);

        Achievement achievement1 = new Achievement();
        achievement1.setTitle("Achievement 1");
        achievement1.setAmountToReach(40f);
        achievement1.setGoal(testGoal);

        Achievement achievement2 = new Achievement();
        achievement2.setTitle("Achievement 2");
        achievement2.setAmountToReach(60f);
        achievement2.setGoal(testGoal);

        achievementRepository.saveAll(List.of(achievement1, achievement2));

        testGoal.getAchievements().addAll(List.of(achievement1, achievement2));
        goalRepository.save(testGoal);

        SessionRequestDto sessionDto = new SessionRequestDto();

        achievementService.checkAchievements(testGoal.getGoalId(), sessionDto);

        Achievement updatedAch1 = achievementRepository.findById(achievement1.getAchievementId()).orElseThrow();
        Achievement updatedAch2 = achievementRepository.findById(achievement2.getAchievementId()).orElseThrow();

        assertNotNull(updatedAch1.getDateAwarded());
        assertNull(updatedAch2.getDateAwarded());
    }

    @Test
    void checkAchievements_ShouldDoNothingWhenNoAchievements() {
        SessionRequestDto sessionDto = new SessionRequestDto();
        achievementService.checkAchievements(testGoal.getGoalId(), sessionDto);
    }
}