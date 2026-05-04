package com.example.winter_olympics.athlete.service;

import com.example.winter_olympics.athlete.dto.AthleteResponse;
import com.example.winter_olympics.athlete.dto.CreateAthleteRequest;
import com.example.winter_olympics.athlete.dto.UpdateAthleteRequest;

import java.util.List;

public interface AthleteService {

    List<AthleteResponse> getAllAthletes();

    AthleteResponse getAthleteById(Long id);

    AthleteResponse createAthlete(CreateAthleteRequest request);

    AthleteResponse updateAthlete(Long id, UpdateAthleteRequest request);

    void deleteAthlete(Long id);
}