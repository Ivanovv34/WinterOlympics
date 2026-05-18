package com.example.winter_olympics.statistics.dto;

import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.result.model.MedalType;

import java.time.LocalDate;

public record MedalistResponse(
        Long athleteId,
        String athleteFullName,
        String country,
        LocalDate birthDate,
        int age,
        Long competitionId,
        String competitionName,
        CompetitionType competitionType,
        Integer rankPosition,
        MedalType medal
) {
}