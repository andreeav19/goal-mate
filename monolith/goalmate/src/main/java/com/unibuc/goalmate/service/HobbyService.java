package com.unibuc.goalmate.service;

import com.unibuc.goalmate.dto.HobbyOptionResponseDto;
import com.unibuc.goalmate.dto.HobbyRequestDto;
import com.unibuc.goalmate.dto.HobbyResponseDto;
import com.unibuc.goalmate.model.Hobby;
import com.unibuc.goalmate.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HobbyService {
    private final HobbyRepository hobbyRepository;

    public List<HobbyResponseDto> getAllHobbies() {
        return hobbyRepository.findAll().stream()
                .map(hobby -> new HobbyResponseDto(
                        hobby.getName(),
                        hobby.getDescription()
                ))
                .collect(Collectors.toList());
    }

    public void addHobby(HobbyRequestDto request) {
        if (hobbyRepository.existsByName(request.getName())) {
            throw new RuntimeException("A hobby with this name already exists.");
        }

        Hobby hobby = new Hobby();
        hobby.setName(request.getName());
        hobby.setDescription(request.getDescription());

        hobbyRepository.save(hobby);
    }

    @Transactional
    public void deleteHobbyByName(String name) {
        if (hobbyRepository.existsByName(name)) {
            hobbyRepository.deleteByName(name);
        } else {
            throw new EntityNotFoundException("Hobby not found.");
        }
    }

    public List<HobbyOptionResponseDto> getHobbyOptions() {
        return hobbyRepository.findAll().stream()
                .map(hobby -> new HobbyOptionResponseDto(
                        hobby.getHobbyId(),
                        hobby.getName()
                ))
                .collect(Collectors.toList());
    }
}
