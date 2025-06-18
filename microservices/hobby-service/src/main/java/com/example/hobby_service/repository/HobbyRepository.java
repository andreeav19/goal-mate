package com.example.hobby_service.repository;

import com.example.hobby_service.model.Hobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HobbyRepository extends JpaRepository<Hobby, Long> {

    void deleteByName(String name);

    boolean existsByName(String name);

    Optional<Hobby> findByName(String name);
}