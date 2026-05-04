package com.example.winter_olympics.registration.repository;

import com.example.winter_olympics.registration.model.CompetitionRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetitionRegistrationRepository extends JpaRepository<CompetitionRegistrationEntity, Long> {

    List<CompetitionRegistrationEntity> findByCompetitionId(Long competitionId);

    List<CompetitionRegistrationEntity> findByAthleteId(Long athleteId);

    boolean existsByCompetitionIdAndAthleteId(Long competitionId, Long athleteId);
}