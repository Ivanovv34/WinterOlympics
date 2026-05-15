package com.example.winter_olympics.registration.dto;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.competition.model.CompetitionType;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long id,
        Long competitionId,
        String competitionName,
        CompetitionType competitionType,
        Long athleteId,
        String athleteFullName,
        String country,
        Gender gender,
        LocalDateTime registeredAt
) {
}