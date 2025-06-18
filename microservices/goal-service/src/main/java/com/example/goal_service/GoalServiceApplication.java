package com.example.goal_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.goal_service.feign")
public class GoalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoalServiceApplication.class, args);
	}

}
