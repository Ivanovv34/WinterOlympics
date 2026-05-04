package com.example.winter_olympics.athlete.repository;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AthleteRepository extends JpaRepository<AthleteEntity, Long> {

    Optional<AthleteEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}