package com.example.winter_olympics.competition.dto;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;

import java.time.LocalDate;

public record CompetitionResponse(
        Long id,
        String name,
        CompetitionType type,
        Gender gender,
        Integer minAge,
        LocalDate competitionDate,
        CompetitionStatus status
) {
}