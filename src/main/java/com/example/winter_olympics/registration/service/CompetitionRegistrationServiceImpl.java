package com.example.winter_olympics.registration.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import com.example.winter_olympics.registration.dto.RegistrationResponse;
import com.example.winter_olympics.registration.model.CompetitionRegistrationEntity;
import com.example.winter_olympics.registration.repository.CompetitionRegistrationRepository;
import com.example.winter_olympics.user.model.UserEntity;
import com.example.winter_olympics.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionRegistrationServiceImpl implements CompetitionRegistrationService {

    private final CompetitionRegistrationRepository registrationRepository;
    private final CompetitionRepository competitionRepository;
    private final AthleteRepository athleteRepository;
    private final UserRepository userRepository;

    @Override
    public RegistrationResponse registerAthlete(Long competitionId, Long athleteId) {
        CompetitionEntity competition = findCompetitionById(competitionId);
        AthleteEntity athlete = findAthleteById(athleteId);

        validateRegistration(competition, athlete);

        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setCompetition(competition);
        registration.setAthlete(athlete);

        return mapToResponse(registrationRepository.save(registration));
    }

    @Override
    public RegistrationResponse registerMe(Long competitionId) {
        CompetitionEntity competition = findCompetitionById(competitionId);
        AthleteEntity athlete = getCurrentUserAthlete();

        validateRegistration(competition, athlete);

        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setCompetition(competition);
        registration.setAthlete(athlete);

        return mapToResponse(registrationRepository.save(registration));
    }

    @Override
    public void unregisterAthlete(Long competitionId, Long athleteId) {
        CompetitionRegistrationEntity registration = registrationRepository
                .findByCompetitionIdAndAthleteId(competitionId, athleteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.REGISTRATION_NOT_FOUND.formatted(competitionId, athleteId)
                ));

        registrationRepository.delete(registration);
    }

    @Override
    public void unregisterMe(Long competitionId) {
        AthleteEntity athlete = getCurrentUserAthlete();

        CompetitionRegistrationEntity registration = registrationRepository
                .findByCompetitionIdAndAthleteId(competitionId, athlete.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.REGISTRATION_NOT_FOUND.formatted(competitionId, athlete.getId())
                ));

        registrationRepository.delete(registration);
    }

    @Override
    public List<RegistrationResponse> getRegistrationsByCompetition(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new ResourceNotFoundException(
                    ErrorMessages.COMPETITION_NOT_FOUND.formatted(competitionId)
            );
        }

        return registrationRepository.findByCompetitionId(competitionId)
                .stream()
                .sorted(Comparator.comparing(r -> r.getAthlete().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RegistrationResponse> getRegistrationsByAthlete(Long athleteId) {
        if (!athleteRepository.existsById(athleteId)) {
            throw new ResourceNotFoundException(
                    ErrorMessages.ATHLETE_NOT_FOUND.formatted(athleteId)
            );
        }

        return registrationRepository.findByAthleteId(athleteId)
                .stream()
                .sorted(Comparator.comparing(r -> r.getCompetition().getCompetitionDate()))
                .map(this::mapToResponse)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private AthleteEntity getCurrentUserAthlete() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        return athleteRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "You don't have an athlete profile yet. Please register your athlete profile first."
                ));
    }

    private void validateRegistration(CompetitionEntity competition, AthleteEntity athlete) {
        if (competition.getStatus() != CompetitionStatus.OPEN) {
            throw new BadRequestException(ErrorMessages.COMPETITION_NOT_OPEN);
        }

        if (registrationRepository.existsByCompetitionIdAndAthleteId(
                competition.getId(),
                athlete.getId()
        )) {
            throw new BadRequestException(
                    ErrorMessages.ATHLETE_ALREADY_REGISTERED.formatted(
                            athlete.getId(),
                            competition.getId()
                    )
            );
        }

        if (athlete.getGender() != competition.getGender()) {
            throw new BadRequestException(ErrorMessages.ATHLETE_GENDER_DOES_NOT_MATCH);
        }

        int athleteAge = calculateAge(athlete.getBirthDate(), competition.getCompetitionDate());

        if (athleteAge < competition.getMinAge()) {
            throw new BadRequestException(ErrorMessages.ATHLETE_DOES_NOT_MEET_MINIMUM_AGE);
        }
    }

    private int calculateAge(LocalDate birthDate, LocalDate competitionDate) {
        return Period.between(birthDate, competitionDate).getYears();
    }

    private CompetitionEntity findCompetitionById(Long competitionId) {
        return competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.COMPETITION_NOT_FOUND.formatted(competitionId)
                ));
    }

    private AthleteEntity findAthleteById(Long athleteId) {
        return athleteRepository.findById(athleteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.ATHLETE_NOT_FOUND.formatted(athleteId)
                ));
    }

    private RegistrationResponse mapToResponse(CompetitionRegistrationEntity registration) {
        AthleteEntity athlete = registration.getAthlete();
        CompetitionEntity competition = registration.getCompetition();

        return new RegistrationResponse(
                registration.getId(),
                competition.getId(),
                competition.getName(),
                competition.getType(),
                athlete.getId(),
                athlete.getFirstName() + " " + athlete.getLastName(),
                athlete.getCountry(),
                athlete.getGender(),
                registration.getRegisteredAt()
        );
    }
}