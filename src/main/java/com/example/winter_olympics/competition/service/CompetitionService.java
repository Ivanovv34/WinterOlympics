package com.example.winter_olympics.competition.service;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.competition.dto.CompetitionResponse;
import com.example.winter_olympics.competition.dto.CreateCompetitionRequest;
import com.example.winter_olympics.competition.dto.UpdateCompetitionRequest;
import com.example.winter_olympics.competition.model.CompetitionType;

import java.util.List;

public interface CompetitionService {

    List<CompetitionResponse> getAllCompetitions(CompetitionType type, Gender gender);

    CompetitionResponse getCompetitionById(Long id);

    CompetitionResponse createCompetition(CreateCompetitionRequest request);

    CompetitionResponse updateCompetition(Long id, UpdateCompetitionRequest request);

    void deleteCompetition(Long id);
}