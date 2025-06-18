package com.example.goal_service.feign;

import com.example.goal_service.dto.HobbyDto;
import com.example.goal_service.dto.HobbyOptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "hobby-service", configuration = FeignClientConfig.class)
public interface HobbyClient {

    @GetMapping("/hobbies/getHobby/{id}")
    HobbyDto getHobbyById(@PathVariable Long id);

    @GetMapping("/hobbies/options")
    List<HobbyOptionDto> getHobbyOptions();
}


