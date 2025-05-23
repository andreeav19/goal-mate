package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.SessionRequestDto;
import com.unibuc.goalmate.model.Goal;
import com.unibuc.goalmate.model.GoalMateUser;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.model.Session;
import com.unibuc.goalmate.repository.GoalMateUserRepository;
import com.unibuc.goalmate.repository.GoalRepository;
import com.unibuc.goalmate.repository.HobbyRepository;
import com.unibuc.goalmate.repository.SessionRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class SessionServiceIntegrationTest {
    @Autowired
    private SessionService sessionService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private GoalMateUserRepository userRepository;

    @Autowired
    private HobbyRepository hobbyRepository;

    private Goal goal;

    @BeforeEach
    void setup() {
        GoalMateUser user = new GoalMateUser();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("pass");
        userRepository.save(user);

        Hobby hobby = new Hobby();
        hobby.setName("Test Hobby");
        hobby.setDescription("desc");
        hobbyRepository.save(hobby);

        goal = new Goal();
        goal.setDescription("Test Goal");
        goal.setCurrentAmount(0f);
        goal.setTargetAmount(100f);
        goal.setTargetUnit("units");
        goal.setDeadline(LocalDate.now().plusDays(10));
        goal.setUser(user);
        goal.setHobby(hobby);
        goal.setSessions(new ArrayList<>());
        goalRepository.save(goal);
    }

    @Test
    void addSessionToGoal_ShouldSaveSessionAndUpdateGoal() {
        SessionRequestDto dto = new SessionRequestDto();
        dto.setProgressAmount(20f);

        sessionService.addSessionToGoal(goal.getGoalId(), dto);
        Goal updated = goalRepository.findById(goal.getGoalId()).orElseThrow();

        assertEquals(20f, updated.getCurrentAmount());
        assertFalse(updated.getSessions().isEmpty());

        Session session = updated.getSessions().getFirst();
        assertEquals(20f, session.getProgressAmount());
        assertEquals(LocalDate.now(), session.getDate());
    }

    @Test
    void addSessionToGoal_ShouldNotAddIfTargetReached() {
        goal.setCurrentAmount(100f);
        goalRepository.save(goal);

        SessionRequestDto dto = new SessionRequestDto();
        dto.setProgressAmount(10f);

        sessionService.addSessionToGoal(goal.getGoalId(), dto);

        Goal updated = goalRepository.findById(goal.getGoalId()).orElseThrow();
        assertEquals(100f, updated.getCurrentAmount());
        assertTrue(updated.getSessions() == null || updated.getSessions().isEmpty());
    }

    @Test
    void deleteSessionFromGoal_ShouldRemoveSessionAndUpdateGoal() {
        Session session = new Session();
        session.setDate(LocalDate.now());
        session.setProgressAmount(30f);
        session.setGoal(goal);
        sessionRepository.save(session);

        goal.setCurrentAmount(30f);
        goal.getSessions().add(session);
        goalRepository.save(goal);

        sessionService.deleteSessionFromGoal(goal.getGoalId(), session.getSessionId());

        Goal updated = goalRepository.findById(goal.getGoalId()).orElseThrow();
        assertEquals(0f, updated.getCurrentAmount());
        assertTrue(updated.getSessions().isEmpty());
        assertFalse(sessionRepository.findById(session.getSessionId()).isPresent());
    }

    @Test
    void deleteSessionFromGoal_ShouldThrowIfSessionDoesNotBelongToGoal() {
        Goal anotherGoal = new Goal();
        anotherGoal.setDescription("Another goal");
        anotherGoal.setCurrentAmount(15f);
        anotherGoal.setTargetAmount(50f);
        anotherGoal.setTargetUnit("units");
        anotherGoal.setDeadline(LocalDate.now().plusDays(5));
        anotherGoal.setUser(goal.getUser());
        anotherGoal.setHobby(goal.getHobby());
        goalRepository.save(anotherGoal);

        Session session = new Session();
        session.setDate(LocalDate.now());
        session.setProgressAmount(15f);
        session.setGoal(anotherGoal);
        sessionRepository.save(session);

        Long sessionId = session.getSessionId();
        Long goalId = goal.getGoalId();

        assertThrows(IllegalArgumentException.class,
                () -> sessionService.deleteSessionFromGoal(goalId, sessionId));
    }

    @Test
    void deleteSessionFromGoal_ShouldThrowWhenSessionAfterDeadline() {
        Session session = new Session();
        session.setDate(goal.getDeadline().plusDays(1));
        session.setProgressAmount(10f);
        session.setGoal(goal);
        sessionRepository.save(session);

        goal.setCurrentAmount(10f);
        goal.getSessions().add(session);
        goalRepository.save(goal);

        assertThrows(RuntimeException.class,
                () -> sessionService.deleteSessionFromGoal(goal.getGoalId(), session.getSessionId()));
    }
}