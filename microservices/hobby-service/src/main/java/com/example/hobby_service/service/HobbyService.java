package com.example.hobby_service.service;

import com.example.hobby_service.dto.HobbyDto;
import com.example.hobby_service.dto.HobbyOptionResponseDto;
import com.example.hobby_service.dto.HobbyRequestDto;
import com.example.hobby_service.dto.HobbyResponseDto;
import com.example.hobby_service.model.Hobby;
import com.example.hobby_service.repository.HobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HobbyService {
    private final HobbyRepository hobbyRepository;

    public Page<HobbyResponseDto> getAllHobbies(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return hobbyRepository.findAll(pageable).map(hobby ->
                new HobbyResponseDto(hobby.getName(), hobby.getDescription())
        );
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

    public HobbyDto findHobbyById(Long id) {
        Hobby hobby = hobbyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hobby not found with id: " + id));

        return new HobbyDto(hobby.getHobbyId(), hobby.getName(), hobby.getDescription());
    }
}

