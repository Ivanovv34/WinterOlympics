package com.example.winter_olympics.result.dto;

import com.example.winter_olympics.result.model.MedalType;

import java.math.BigDecimal;

public record BiathlonResultResponse(
        Long id,
        Long competitionId,
        String competitionName,
        Long athleteId,
        String athleteFullName,
        String country,
        BigDecimal skiTime,
        Integer missedShots,
        BigDecimal penaltySeconds,
        BigDecimal finalTime,
        boolean didNotFinish,
        Integer rankPosition,
        MedalType medal
) {
}