package com.example.winter_olympics.result.repository;

import com.example.winter_olympics.result.model.BiathlonResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BiathlonResultRepository extends JpaRepository<BiathlonResultEntity, Long> {

    List<BiathlonResultEntity> findByCompetitionId(Long competitionId);

    Optional<BiathlonResultEntity> findByCompetitionIdAndAthleteId(Long competitionId, Long athleteId);

    boolean existsByCompetitionIdAndAthleteId(Long competitionId, Long athleteId);

    List<BiathlonResultEntity> findByCompetitionIdOrderByFinalTimeAsc(Long competitionId);
}