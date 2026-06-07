package com.example.winter_olympics.registration.service;

import com.example.winter_olympics.registration.dto.RegistrationResponse;

import java.util.List;

public interface CompetitionRegistrationService {

    RegistrationResponse registerAthlete(Long competitionId, Long athleteId);

    RegistrationResponse registerMe(Long competitionId);

    void unregisterAthlete(Long competitionId, Long athleteId);

    void unregisterMe(Long competitionId);

    List<RegistrationResponse> getRegistrationsByCompetition(Long competitionId);

    List<RegistrationResponse> getRegistrationsByAthlete(Long athleteId);
}