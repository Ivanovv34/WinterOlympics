package com.example.winter_olympics.statistics.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.registration.model.CompetitionRegistrationEntity;
import com.example.winter_olympics.registration.repository.CompetitionRegistrationRepository;
import com.example.winter_olympics.result.model.BiathlonResultEntity;
import com.example.winter_olympics.result.model.MedalType;
import com.example.winter_olympics.result.model.SlalomResultEntity;
import com.example.winter_olympics.result.repository.BiathlonResultRepository;
import com.example.winter_olympics.result.repository.SlalomResultRepository;
import com.example.winter_olympics.statistics.dto.AverageAgeResponse;
import com.example.winter_olympics.statistics.dto.CountryMedalResponse;
import com.example.winter_olympics.statistics.dto.MedalistResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OlympicStatisticsServiceImplTest {

    @Mock
    private SlalomResultRepository slalomResultRepository;

    @Mock
    private BiathlonResultRepository biathlonResultRepository;

    @Mock
    private CompetitionRegistrationRepository registrationRepository;

    @InjectMocks
    private OlympicStatisticsServiceImpl statisticsService;

    @Test
    void getMedalsByCountryShouldReturnSortedCountryMedals() {
        CompetitionEntity slalomCompetition = createCompetition(1L, CompetitionType.SLALOM);
        CompetitionEntity biathlonCompetition = createCompetition(2L, CompetitionType.BIATHLON);

        AthleteEntity swissAthlete = createAthlete(1L, "Marco", "Odermatt", "Switzerland", LocalDate.of(1997, 10, 8));
        AthleteEntity norwegianAthlete = createAthlete(2L, "Johannes", "Boe", "Norway", LocalDate.of(1993, 5, 16));
        AthleteEntity frenchAthlete = createAthlete(3L, "Quentin", "Fillon Maillet", "France", LocalDate.of(1992, 8, 16));

        SlalomResultEntity slalomGold = createSlalomResult(1L, slalomCompetition, swissAthlete, MedalType.GOLD);
        SlalomResultEntity slalomBronze = createSlalomResult(2L, slalomCompetition, frenchAthlete, MedalType.BRONZE);
        BiathlonResultEntity biathlonSilver = createBiathlonResult(3L, biathlonCompetition, norwegianAthlete, MedalType.SILVER);
        BiathlonResultEntity biathlonGold = createBiathlonResult(4L, biathlonCompetition, norwegianAthlete, MedalType.GOLD);

        when(slalomResultRepository.findAll()).thenReturn(List.of(slalomGold, slalomBronze));
        when(biathlonResultRepository.findAll()).thenReturn(List.of(biathlonSilver, biathlonGold));

        List<CountryMedalResponse> response = statisticsService.getMedalsByCountry();

        assertThat(response).hasSize(3);

        assertThat(response.get(0).country()).isEqualTo("Norway");
        assertThat(response.get(0).goldMedals()).isEqualTo(1);
        assertThat(response.get(0).silverMedals()).isEqualTo(1);
        assertThat(response.get(0).bronzeMedals()).isEqualTo(0);
        assertThat(response.get(0).totalMedals()).isEqualTo(2);

        assertThat(response.get(1).country()).isEqualTo("Switzerland");
        assertThat(response.get(1).goldMedals()).isEqualTo(1);
        assertThat(response.get(1).totalMedals()).isEqualTo(1);

        assertThat(response.get(2).country()).isEqualTo("France");
        assertThat(response.get(2).bronzeMedals()).isEqualTo(1);
        assertThat(response.get(2).totalMedals()).isEqualTo(1);
    }

    @Test
    void getAverageAgeShouldReturnAverageAgeOfDistinctRegisteredAthletes() {
        AthleteEntity athleteOne = createAthlete(1L, "Marco", "Odermatt", "Switzerland", LocalDate.of(1997, 10, 8));
        AthleteEntity athleteTwo = createAthlete(2L, "Johannes", "Boe", "Norway", LocalDate.of(1993, 5, 16));

        CompetitionEntity competition = createCompetition(1L, CompetitionType.SLALOM);

        CompetitionRegistrationEntity registrationOne = createRegistration(1L, competition, athleteOne);
        CompetitionRegistrationEntity registrationTwo = createRegistration(2L, competition, athleteTwo);

        when(registrationRepository.findAll()).thenReturn(List.of(registrationOne, registrationTwo));

        AverageAgeResponse response = statisticsService.getAverageAge();

        int ageOne = Period.between(athleteOne.getBirthDate(), LocalDate.now()).getYears();
        int ageTwo = Period.between(athleteTwo.getBirthDate(), LocalDate.now()).getYears();

        BigDecimal expectedAverage = BigDecimal.valueOf(ageOne + ageTwo)
                .divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);

        assertThat(response.participantsCount()).isEqualTo(2);
        assertThat(response.averageAge()).isEqualByComparingTo(expectedAverage);
    }

    @Test
    void getAverageAgeShouldReturnZeroWhenThereAreNoRegistrations() {
        when(registrationRepository.findAll()).thenReturn(List.of());

        AverageAgeResponse response = statisticsService.getAverageAge();

        assertThat(response.participantsCount()).isZero();
        assertThat(response.averageAge()).isEqualByComparingTo("0.00");
    }

    @Test
    void getYoungestMedalistShouldReturnYoungestMedalist() {
        CompetitionEntity competition = createCompetition(1L, CompetitionType.SLALOM);

        AthleteEntity olderAthlete = createAthlete(1L, "Johannes", "Boe", "Norway", LocalDate.of(1993, 5, 16));
        AthleteEntity youngerAthlete = createAthlete(2L, "Marco", "Odermatt", "Switzerland", LocalDate.of(1997, 10, 8));

        SlalomResultEntity olderResult = createSlalomResult(1L, competition, olderAthlete, MedalType.GOLD);
        SlalomResultEntity youngerResult = createSlalomResult(2L, competition, youngerAthlete, MedalType.SILVER);

        when(slalomResultRepository.findAll()).thenReturn(List.of(olderResult, youngerResult));
        when(biathlonResultRepository.findAll()).thenReturn(List.of());

        MedalistResponse response = statisticsService.getYoungestMedalist();

        assertThat(response.athleteId()).isEqualTo(2L);
        assertThat(response.athleteFullName()).isEqualTo("Marco Odermatt");
        assertThat(response.medal()).isEqualTo(MedalType.SILVER);
    }

    @Test
    void getOldestMedalistShouldReturnOldestMedalist() {
        CompetitionEntity competition = createCompetition(1L, CompetitionType.SLALOM);

        AthleteEntity olderAthlete = createAthlete(1L, "Johannes", "Boe", "Norway", LocalDate.of(1993, 5, 16));
        AthleteEntity youngerAthlete = createAthlete(2L, "Marco", "Odermatt", "Switzerland", LocalDate.of(1997, 10, 8));

        SlalomResultEntity olderResult = createSlalomResult(1L, competition, olderAthlete, MedalType.GOLD);
        SlalomResultEntity youngerResult = createSlalomResult(2L, competition, youngerAthlete, MedalType.SILVER);

        when(slalomResultRepository.findAll()).thenReturn(List.of(olderResult, youngerResult));
        when(biathlonResultRepository.findAll()).thenReturn(List.of());

        MedalistResponse response = statisticsService.getOldestMedalist();

        assertThat(response.athleteId()).isEqualTo(1L);
        assertThat(response.athleteFullName()).isEqualTo("Johannes Boe");
        assertThat(response.medal()).isEqualTo(MedalType.GOLD);
    }

    @Test
    void getYoungestMedalistShouldThrowWhenNoMedalistsExist() {
        when(slalomResultRepository.findAll()).thenReturn(List.of());
        when(biathlonResultRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> statisticsService.getYoungestMedalist())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There are no medalists yet");
    }

    private CompetitionEntity createCompetition(Long id, CompetitionType type) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setId(id);
        competition.setName(type == CompetitionType.SLALOM ? "Men Slalom 2026" : "Men Biathlon 2026");
        competition.setType(type);
        competition.setGender(Gender.MALE);
        competition.setMinAge(18);
        competition.setCompetitionDate(LocalDate.of(2026, 6, 1));
        competition.setStatus(CompetitionStatus.RESULTS_COMPLETED);

        return competition;
    }

    private AthleteEntity createAthlete(
            Long id,
            String firstName,
            String lastName,
            String country,
            LocalDate birthDate
    ) {
        AthleteEntity athlete = new AthleteEntity();
        athlete.setId(id);
        athlete.setFirstName(firstName);
        athlete.setLastName(lastName);
        athlete.setCountry(country);
        athlete.setGender(Gender.MALE);
        athlete.setBirthDate(birthDate);

        return athlete;
    }

    private CompetitionRegistrationEntity createRegistration(
            Long id,
            CompetitionEntity competition,
            AthleteEntity athlete
    ) {
        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setId(id);
        registration.setCompetition(competition);
        registration.setAthlete(athlete);
        registration.setRegisteredAt(LocalDateTimeStub.now());

        return registration;
    }

    private SlalomResultEntity createSlalomResult(
            Long id,
            CompetitionEntity competition,
            AthleteEntity athlete,
            MedalType medal
    ) {
        SlalomResultEntity result = new SlalomResultEntity();
        result.setId(id);
        result.setCompetition(competition);
        result.setAthlete(athlete);
        result.setRankPosition(getRankByMedal(medal));
        result.setMedal(medal);

        return result;
    }

    private BiathlonResultEntity createBiathlonResult(
            Long id,
            CompetitionEntity competition,
            AthleteEntity athlete,
            MedalType medal
    ) {
        BiathlonResultEntity result = new BiathlonResultEntity();
        result.setId(id);
        result.setCompetition(competition);
        result.setAthlete(athlete);
        result.setRankPosition(getRankByMedal(medal));
        result.setMedal(medal);

        return result;
    }

    private int getRankByMedal(MedalType medal) {
        return switch (medal) {
            case GOLD -> 1;
            case SILVER -> 2;
            case BRONZE -> 3;
            case NONE -> 0;
        };
    }

    private static final class LocalDateTimeStub {
        private static java.time.LocalDateTime now() {
            return java.time.LocalDateTime.now();
        }
    }
}