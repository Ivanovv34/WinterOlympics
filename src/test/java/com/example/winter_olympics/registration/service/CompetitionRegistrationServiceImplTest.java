package com.example.winter_olympics.registration.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import com.example.winter_olympics.registration.dto.RegistrationResponse;
import com.example.winter_olympics.registration.model.CompetitionRegistrationEntity;
import com.example.winter_olympics.registration.repository.CompetitionRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionRegistrationServiceImplTest {

    @Mock
    private CompetitionRegistrationRepository registrationRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private CompetitionRegistrationServiceImpl registrationService;

    @Test
    void registerAthleteShouldCreateRegistrationWhenDataIsValid() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createCompetition(competitionId);
        AthleteEntity athlete = createAthlete(athleteId);

        CompetitionRegistrationEntity savedRegistration = new CompetitionRegistrationEntity();
        savedRegistration.setId(10L);
        savedRegistration.setCompetition(competition);
        savedRegistration.setAthlete(athlete);
        savedRegistration.setRegisteredAt(LocalDateTime.now());

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);
        when(registrationRepository.save(any(CompetitionRegistrationEntity.class))).thenReturn(savedRegistration);

        RegistrationResponse response = registrationService.registerAthlete(competitionId, athleteId);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.competitionId()).isEqualTo(competitionId);
        assertThat(response.athleteId()).isEqualTo(athleteId);
        assertThat(response.athleteFullName()).isEqualTo("Marco Odermatt");
        assertThat(response.country()).isEqualTo("Switzerland");

        verify(registrationRepository).save(any(CompetitionRegistrationEntity.class));
    }

    @Test
    void registerAthleteShouldThrowWhenCompetitionIsNotOpen() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createCompetition(competitionId);
        competition.setStatus(CompetitionStatus.RESULTS_COMPLETED);

        AthleteEntity athlete = createAthlete(athleteId);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> registrationService.registerAthlete(competitionId, athleteId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Competition is not open for registrations");

        verify(registrationRepository, never()).save(any(CompetitionRegistrationEntity.class));
    }

    @Test
    void registerAthleteShouldThrowWhenAthleteAlreadyRegistered() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createCompetition(competitionId);
        AthleteEntity athlete = createAthlete(athleteId);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerAthlete(competitionId, athleteId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Athlete with id 2 is already registered for competition with id 1");

        verify(registrationRepository, never()).save(any(CompetitionRegistrationEntity.class));
    }

    @Test
    void registerAthleteShouldThrowWhenGenderDoesNotMatch() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createCompetition(competitionId);
        competition.setGender(Gender.FEMALE);

        AthleteEntity athlete = createAthlete(athleteId);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);

        assertThatThrownBy(() -> registrationService.registerAthlete(competitionId, athleteId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Athlete gender does not match competition gender");

        verify(registrationRepository, never()).save(any(CompetitionRegistrationEntity.class));
    }

    @Test
    void registerAthleteShouldThrowWhenAthleteDoesNotMeetMinimumAge() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createCompetition(competitionId);
        competition.setMinAge(18);

        AthleteEntity athlete = createAthlete(athleteId);
        athlete.setBirthDate(LocalDate.of(2012, 1, 1));

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);

        assertThatThrownBy(() -> registrationService.registerAthlete(competitionId, athleteId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Athlete does not meet the minimum age requirement");

        verify(registrationRepository, never()).save(any(CompetitionRegistrationEntity.class));
    }

    @Test
    void unregisterAthleteShouldDeleteRegistrationWhenItExists() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setId(10L);
        registration.setCompetition(createCompetition(competitionId));
        registration.setAthlete(createAthlete(athleteId));

        when(registrationRepository.findByCompetitionIdAndAthleteId(competitionId, athleteId))
                .thenReturn(Optional.of(registration));

        registrationService.unregisterAthlete(competitionId, athleteId);

        verify(registrationRepository).delete(registration);
    }

    @Test
    void unregisterAthleteShouldThrowWhenRegistrationDoesNotExist() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        when(registrationRepository.findByCompetitionIdAndAthleteId(competitionId, athleteId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.unregisterAthlete(competitionId, athleteId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Registration for competition id 1 and athlete id 2 was not found");

        verify(registrationRepository, never()).delete(any(CompetitionRegistrationEntity.class));
    }

    private CompetitionEntity createCompetition(Long id) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setId(id);
        competition.setName("Men Slalom 2026");
        competition.setType(CompetitionType.SLALOM);
        competition.setGender(Gender.MALE);
        competition.setMinAge(18);
        competition.setCompetitionDate(LocalDate.of(2026, 6, 1));
        competition.setStatus(CompetitionStatus.OPEN);

        return competition;
    }

    private AthleteEntity createAthlete(Long id) {
        AthleteEntity athlete = new AthleteEntity();
        athlete.setId(id);
        athlete.setFirstName("Marco");
        athlete.setLastName("Odermatt");
        athlete.setCountry("Switzerland");
        athlete.setGender(Gender.MALE);
        athlete.setBirthDate(LocalDate.of(1997, 10, 8));

        return athlete;
    }
}