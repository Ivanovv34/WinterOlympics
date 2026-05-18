package com.example.winter_olympics.result.repository;

import com.example.winter_olympics.result.model.SlalomResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlalomResultRepository extends JpaRepository<SlalomResultEntity, Long> {

    List<SlalomResultEntity> findByCompetitionId(Long competitionId);

    Optional<SlalomResultEntity> findByCompetitionIdAndAthleteId(Long competitionId, Long athleteId);

    boolean existsByCompetitionIdAndAthleteId(Long competitionId, Long athleteId);

    List<SlalomResultEntity> findByCompetitionIdOrderByFirstRunTimeAsc(Long competitionId);

    List<SlalomResultEntity> findByCompetitionIdAndQualifiedForSecondRunTrueOrderByFirstRunTimeDesc(Long competitionId);

    List<SlalomResultEntity> findByCompetitionIdOrderByTotalTimeAsc(Long competitionId);

    List<SlalomResultEntity> findByCompetitionIdAndQualifiedForSecondRunTrueAndTotalTimeIsNotNullOrderByTotalTimeAsc(
            Long competitionId
    );
}