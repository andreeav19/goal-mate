package com.unibuc.goalmate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "dev"})
class GoalMateApplicationTests {

	@Test
	void contextLoads() {}
}

