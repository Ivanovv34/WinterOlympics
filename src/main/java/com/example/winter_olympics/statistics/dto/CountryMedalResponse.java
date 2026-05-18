package com.example.winter_olympics.statistics.dto;

public record CountryMedalResponse(
        String country,
        int goldMedals,
        int silverMedals,
        int bronzeMedals,
        int totalMedals
) {
}