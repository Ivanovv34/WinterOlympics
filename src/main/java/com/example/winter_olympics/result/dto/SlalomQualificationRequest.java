package com.example.winter_olympics.result.dto;

import com.example.winter_olympics.common.constants.ValidationMessages;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SlalomQualificationRequest(

        @NotNull(message = ValidationMessages.QUALIFICATION_LIMIT_REQUIRED)
        @Min(value = 1, message = ValidationMessages.QUALIFICATION_LIMIT_MIN)
        Integer qualificationLimit
) {
}