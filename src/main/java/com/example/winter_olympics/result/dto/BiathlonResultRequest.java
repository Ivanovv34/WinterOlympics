package com.example.winter_olympics.result.dto;

import com.example.winter_olympics.common.constants.ValidationMessages;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BiathlonResultRequest(

        @NotNull(message = ValidationMessages.ATHLETE_ID_REQUIRED)
        Long athleteId,

        @DecimalMin(value = "0.001", message = ValidationMessages.SKI_TIME_POSITIVE)
        BigDecimal skiTime,

        @NotNull(message = ValidationMessages.MISSED_SHOTS_REQUIRED)
        @Min(value = 0, message = ValidationMessages.MISSED_SHOTS_MIN)
        @Max(value = 100, message = ValidationMessages.MISSED_SHOTS_MAX)
        Integer missedShots,

        boolean didNotFinish
) {
}