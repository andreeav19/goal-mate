package com.example.goal_service.client;

import com.example.goal_service.dto.HobbyDto;
import com.example.goal_service.dto.HobbyOptionDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class HobbyClientFallback implements HobbyClient {

    @Override
    public HobbyDto getHobbyById(Long id) {
        return new HobbyDto(id, "Fallback Hobby", null);
    }

    @Override
    public List<HobbyOptionDto> getHobbyOptions() {
        return Collections.singletonList(new HobbyOptionDto("Fallback Option"));
    }
}