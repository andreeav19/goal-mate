package com.example.hobby_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class HobbyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HobbyServiceApplication.class, args);
	}

}
