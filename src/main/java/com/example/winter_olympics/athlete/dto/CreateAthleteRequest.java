package com.example.winter_olympics.athlete.dto;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.common.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAthleteRequest(

        @NotBlank(message = ValidationMessages.FIRST_NAME_REQUIRED)
        @Size(min = 2, max = 100, message = ValidationMessages.FIRST_NAME_MIN_LENGTH)
        String firstName,

        @NotBlank(message = ValidationMessages.LAST_NAME_REQUIRED)
        @Size(min = 2, max = 100, message = ValidationMessages.LAST_NAME_MIN_LENGTH)
        String lastName,

        @NotBlank(message = ValidationMessages.COUNTRY_REQUIRED)
        @Size(min = 2, max = 100, message = ValidationMessages.COUNTRY_MIN_LENGTH)
        String country,

        @NotNull(message = ValidationMessages.GENDER_REQUIRED)
        Gender gender,

        @NotNull(message = ValidationMessages.BIRTH_DATE_REQUIRED)
        @Past(message = ValidationMessages.BIRTH_DATE_PAST)
        LocalDate birthDate
) {
}