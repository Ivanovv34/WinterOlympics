package com.example.winter_olympics.result.dto;

import com.example.winter_olympics.result.model.MedalType;

import java.math.BigDecimal;

public record SlalomResultResponse(
        Long id,
        Long competitionId,
        String competitionName,
        Long athleteId,
        String athleteFullName,
        String country,
        BigDecimal firstRunTime,
        BigDecimal secondRunTime,
        boolean didNotFinishFirstRun,
        boolean didNotFinishSecondRun,
        boolean qualifiedForSecondRun,
        BigDecimal totalTime,
        Integer rankPosition,
        MedalType medal
) {
}