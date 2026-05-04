package com.example.winter_olympics.competition.repository;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetitionRepository extends JpaRepository<CompetitionEntity, Long> {

    List<CompetitionEntity> findByType(CompetitionType type);

    List<CompetitionEntity> findByGender(Gender gender);

    List<CompetitionEntity> findByTypeAndGender(CompetitionType type, Gender gender);
}