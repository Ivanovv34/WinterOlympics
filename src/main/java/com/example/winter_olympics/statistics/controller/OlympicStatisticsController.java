package com.example.winter_olympics.statistics.controller;

import com.example.winter_olympics.statistics.dto.AverageAgeResponse;
import com.example.winter_olympics.statistics.dto.CountryMedalResponse;
import com.example.winter_olympics.statistics.dto.MedalistResponse;
import com.example.winter_olympics.statistics.service.OlympicStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class OlympicStatisticsController {

    private final OlympicStatisticsService olympicStatisticsService;

    @GetMapping("/medals-by-country")
    public ResponseEntity<List<CountryMedalResponse>> getMedalsByCountry() {
        return ResponseEntity.ok(olympicStatisticsService.getMedalsByCountry());
    }

    @GetMapping("/average-age")
    public ResponseEntity<AverageAgeResponse> getAverageAge() {
        return ResponseEntity.ok(olympicStatisticsService.getAverageAge());
    }

    @GetMapping("/youngest-medalist")
    public ResponseEntity<MedalistResponse> getYoungestMedalist() {
        return ResponseEntity.ok(olympicStatisticsService.getYoungestMedalist());
    }

    @GetMapping("/oldest-medalist")
    public ResponseEntity<MedalistResponse> getOldestMedalist() {
        return ResponseEntity.ok(olympicStatisticsService.getOldestMedalist());
    }
}