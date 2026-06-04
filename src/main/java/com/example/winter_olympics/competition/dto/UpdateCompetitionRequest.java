package com.example.winter_olympics.competition.dto;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.common.constants.ValidationMessages;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCompetitionRequest(

        @NotBlank(message = ValidationMessages.COMPETITION_NAME_REQUIRED)
        @Size(min = 3, max = 150, message = ValidationMessages.COMPETITION_NAME_MIN_LENGTH)
        String name,

        @NotNull(message = ValidationMessages.COMPETITION_TYPE_REQUIRED)
        CompetitionType type,

        @NotNull(message = ValidationMessages.GENDER_REQUIRED)
        Gender gender,

        @NotNull(message = ValidationMessages.MINIMUM_AGE_REQUIRED)
        @Min(value = 10, message = ValidationMessages.MINIMUM_AGE_AT_LEAST_10)
        @Max(value = 100, message = ValidationMessages.MINIMUM_AGE_MAX)
        Integer minAge,

        @NotNull(message = ValidationMessages.COMPETITION_DATE_REQUIRED)
        @FutureOrPresent(message = ValidationMessages.COMPETITION_DATE_FUTURE_OR_PRESENT)
        LocalDate competitionDate,

        @NotNull(message = ValidationMessages.COMPETITION_STATUS_REQUIRED)
        CompetitionStatus status
) {
}