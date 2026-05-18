package com.example.winter_olympics.statistics.dto;

import java.math.BigDecimal;

public record AverageAgeResponse(
        int participantsCount,
        BigDecimal averageAge
) {
}