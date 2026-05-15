package com.example.winter_olympics.result.dto;

import com.example.winter_olympics.common.constants.ValidationMessages;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SlalomFirstRunRequest(

        @NotNull(message = ValidationMessages.ATHLETE_ID_REQUIRED)
        Long athleteId,

        @DecimalMin(value = "0.001", message = ValidationMessages.FIRST_RUN_TIME_POSITIVE)
        BigDecimal firstRunTime,

        boolean didNotFinish
) {
}