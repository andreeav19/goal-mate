package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.HobbyOptionResponseDto;
import com.unibuc.goalmate.repository.HobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HobbyService {
    private final HobbyRepository hobbyRepository;

    public List<HobbyOptionResponseDto> getHobbyOptions() {
        return hobbyRepository.findAll().stream()
                .map(hobby -> new HobbyOptionResponseDto(
                        hobby.getHobbyId(),
                        hobby.getName()
                ))
                .collect(Collectors.toList());
    }
}
