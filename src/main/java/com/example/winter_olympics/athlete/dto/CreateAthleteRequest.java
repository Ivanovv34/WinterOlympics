package com.example.winter_olympics.athlete.dto;

import com.example.winter_olympics.athlete.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAthleteRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be up to 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be up to 100 characters")
        String lastName,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must be up to 100 characters")
        String country,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
}