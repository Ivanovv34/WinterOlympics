package com.example.winter_olympics.result.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import com.example.winter_olympics.registration.repository.CompetitionRegistrationRepository;
import com.example.winter_olympics.result.dto.BiathlonResultRequest;
import com.example.winter_olympics.result.dto.BiathlonResultResponse;
import com.example.winter_olympics.result.model.BiathlonResultEntity;
import com.example.winter_olympics.result.model.MedalType;
import com.example.winter_olympics.result.repository.BiathlonResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BiathlonResultServiceImplTest {

    @Mock
    private BiathlonResultRepository biathlonResultRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private CompetitionRegistrationRepository registrationRepository;

    @InjectMocks
    private BiathlonResultServiceImpl biathlonResultService;

    @Test
    void enterResultShouldCalculatePenaltyAndFinalTime() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createBiathlonCompetition(competitionId);
        AthleteEntity athlete = createAthlete(athleteId);

        BiathlonResultRequest request = new BiathlonResultRequest(
                athleteId,
                new BigDecimal("1500.500"),
                2,
                false
        );

        BiathlonResultEntity savedResult = new BiathlonResultEntity();
        savedResult.setId(10L);
        savedResult.setCompetition(competition);
        savedResult.setAthlete(athlete);
        savedResult.setSkiTime(new BigDecimal("1500.500"));
        savedResult.setMissedShots(2);
        savedResult.setPenaltySeconds(new BigDecimal("120"));
        savedResult.setFinalTime(new BigDecimal("1620.500"));
        savedResult.setDidNotFinish(false);
        savedResult.setMedal(MedalType.NONE);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(true);
        when(biathlonResultRepository.findByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(Optional.empty());
        when(biathlonResultRepository.save(any(BiathlonResultEntity.class))).thenReturn(savedResult);

        BiathlonResultResponse response = biathlonResultService.enterResult(competitionId, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.athleteId()).isEqualTo(athleteId);
        assertThat(response.skiTime()).isEqualByComparingTo("1500.500");
        assertThat(response.missedShots()).isEqualTo(2);
        assertThat(response.penaltySeconds()).isEqualByComparingTo("120");
        assertThat(response.finalTime()).isEqualByComparingTo("1620.500");
        assertThat(response.medal()).isEqualTo(MedalType.NONE);
    }

    @Test
    void calculateRankingShouldAssignRanksAndMedals() {
        Long competitionId = 1L;

        CompetitionEntity competition = createBiathlonCompetition(competitionId);

        BiathlonResultEntity first = createBiathlonResult(1L, competition, createAthlete(1L), "1520.000");
        BiathlonResultEntity second = createBiathlonResult(2L, competition, createAthlete(2L), "1560.500");
        BiathlonResultEntity third = createBiathlonResult(3L, competition, createAthlete(3L), "1600.250");

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(biathlonResultRepository.findByCompetitionId(competitionId)).thenReturn(List.of(second, third, first));
        when(biathlonResultRepository.saveAll(anyList())).thenReturn(List.of(first, second, third));
        when(competitionRepository.save(any(CompetitionEntity.class))).thenReturn(competition);
        when(biathlonResultRepository.findByCompetitionIdAndFinalTimeIsNotNullOrderByFinalTimeAsc(competitionId))
                .thenReturn(List.of(first, second, third));

        List<BiathlonResultResponse> ranking = biathlonResultService.calculateRanking(competitionId);

        assertThat(ranking).hasSize(3);

        assertThat(first.getRankPosition()).isEqualTo(1);
        assertThat(first.getMedal()).isEqualTo(MedalType.GOLD);

        assertThat(second.getRankPosition()).isEqualTo(2);
        assertThat(second.getMedal()).isEqualTo(MedalType.SILVER);

        assertThat(third.getRankPosition()).isEqualTo(3);
        assertThat(third.getMedal()).isEqualTo(MedalType.BRONZE);

        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.RESULTS_COMPLETED);
    }

    @Test
    void calculateRankingShouldThrowWhenNoValidResultsExist() {
        Long competitionId = 1L;

        CompetitionEntity competition = createBiathlonCompetition(competitionId);

        BiathlonResultEntity dnfResult = createBiathlonResult(1L, competition, createAthlete(1L), null);
        dnfResult.setDidNotFinish(true);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(biathlonResultRepository.findByCompetitionId(competitionId)).thenReturn(List.of(dnfResult));

        assertThatThrownBy(() -> biathlonResultService.calculateRanking(competitionId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("There are no valid biathlon results for ranking");
    }

    private CompetitionEntity createBiathlonCompetition(Long id) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setId(id);
        competition.setName("Men Biathlon 2026");
        competition.setType(CompetitionType.BIATHLON);
        competition.setGender(Gender.MALE);
        competition.setMinAge(18);
        competition.setCompetitionDate(LocalDate.of(2026, 6, 5));
        competition.setStatus(CompetitionStatus.OPEN);

        return competition;
    }

    private AthleteEntity createAthlete(Long id) {
        AthleteEntity athlete = new AthleteEntity();
        athlete.setId(id);
        athlete.setFirstName("Johannes");
        athlete.setLastName("Boe");
        athlete.setCountry("Norway");
        athlete.setGender(Gender.MALE);
        athlete.setBirthDate(LocalDate.of(1993, 5, 16));

        return athlete;
    }

    private BiathlonResultEntity createBiathlonResult(
            Long id,
            CompetitionEntity competition,
            AthleteEntity athlete,
            String finalTime
    ) {
        BiathlonResultEntity result = new BiathlonResultEntity();
        result.setId(id);
        result.setCompetition(competition);
        result.setAthlete(athlete);
        result.setSkiTime(new BigDecimal("1500.000"));
        result.setMissedShots(0);
        result.setPenaltySeconds(BigDecimal.ZERO);
        result.setFinalTime(finalTime != null ? new BigDecimal(finalTime) : null);
        result.setDidNotFinish(false);
        result.setMedal(MedalType.NONE);

        return result;
    }
}