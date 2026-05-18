package com.example.winter_olympics.statistics.service;

import com.example.winter_olympics.statistics.dto.AverageAgeResponse;
import com.example.winter_olympics.statistics.dto.CountryMedalResponse;
import com.example.winter_olympics.statistics.dto.MedalistResponse;

import java.util.List;

public interface OlympicStatisticsService {

    List<CountryMedalResponse> getMedalsByCountry();

    AverageAgeResponse getAverageAge();

    MedalistResponse getYoungestMedalist();

    MedalistResponse getOldestMedalist();
}