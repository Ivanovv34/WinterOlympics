package com.example.winter_olympics.athlete.dto;

import com.example.winter_olympics.athlete.model.Gender;

import java.time.LocalDate;

public record AthleteResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String country,
        Gender gender,
        LocalDate birthDate
) {
}