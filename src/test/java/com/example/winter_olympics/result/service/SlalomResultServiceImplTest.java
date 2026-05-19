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
import com.example.winter_olympics.result.dto.SlalomFirstRunRequest;
import com.example.winter_olympics.result.dto.SlalomQualificationRequest;
import com.example.winter_olympics.result.dto.SlalomResultResponse;
import com.example.winter_olympics.result.model.MedalType;
import com.example.winter_olympics.result.model.SlalomResultEntity;
import com.example.winter_olympics.result.repository.SlalomResultRepository;
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
class SlalomResultServiceImplTest {

    @Mock
    private SlalomResultRepository slalomResultRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private CompetitionRegistrationRepository registrationRepository;

    @InjectMocks
    private SlalomResultServiceImpl slalomResultService;

    @Test
    void enterFirstRunResultShouldSaveResultWhenAthleteIsRegistered() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createSlalomCompetition(competitionId);
        AthleteEntity athlete = createAthlete(athleteId);

        SlalomFirstRunRequest request = new SlalomFirstRunRequest(
                athleteId,
                new BigDecimal("55.321"),
                false
        );

        SlalomResultEntity savedResult = new SlalomResultEntity();
        savedResult.setId(10L);
        savedResult.setCompetition(competition);
        savedResult.setAthlete(athlete);
        savedResult.setFirstRunTime(new BigDecimal("55.321"));
        savedResult.setDidNotFinishFirstRun(false);
        savedResult.setQualifiedForSecondRun(false);
        savedResult.setMedal(MedalType.NONE);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(true);
        when(slalomResultRepository.findByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(Optional.empty());
        when(slalomResultRepository.save(any(SlalomResultEntity.class))).thenReturn(savedResult);

        SlalomResultResponse response = slalomResultService.enterFirstRunResult(competitionId, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.competitionId()).isEqualTo(competitionId);
        assertThat(response.athleteId()).isEqualTo(athleteId);
        assertThat(response.firstRunTime()).isEqualByComparingTo("55.321");
        assertThat(response.didNotFinishFirstRun()).isFalse();
    }

    @Test
    void enterFirstRunResultShouldThrowWhenAthleteIsNotRegistered() {
        Long competitionId = 1L;
        Long athleteId = 2L;

        CompetitionEntity competition = createSlalomCompetition(competitionId);
        AthleteEntity athlete = createAthlete(athleteId);

        SlalomFirstRunRequest request = new SlalomFirstRunRequest(
                athleteId,
                new BigDecimal("55.321"),
                false
        );

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);

        assertThatThrownBy(() -> slalomResultService.enterFirstRunResult(competitionId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Athlete with id 2 is not registered for competition with id 1");

        verify(slalomResultRepository, never()).save(any(SlalomResultEntity.class));
    }

    @Test
    void qualifyForSecondRunShouldMarkFastestAthletesAsQualified() {
        Long competitionId = 1L;

        CompetitionEntity competition = createSlalomCompetition(competitionId);

        SlalomResultEntity first = createSlalomResult(1L, competition, createAthlete(1L), "54.800");
        SlalomResultEntity second = createSlalomResult(2L, competition, createAthlete(2L), "55.321");
        SlalomResultEntity third = createSlalomResult(3L, competition, createAthlete(3L), "56.900");

        SlalomQualificationRequest request = new SlalomQualificationRequest(2);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(slalomResultRepository.findByCompetitionId(competitionId)).thenReturn(List.of(third, second, first));
        when(competitionRepository.save(any(CompetitionEntity.class))).thenReturn(competition);
        when(slalomResultRepository.saveAll(anyList())).thenReturn(List.of(first, second, third));
        when(slalomResultRepository.findByCompetitionIdAndQualifiedForSecondRunTrueOrderByFirstRunTimeDesc(competitionId))
                .thenReturn(List.of(second, first));

        List<SlalomResultResponse> startList = slalomResultService.qualifyForSecondRun(competitionId, request);

        assertThat(startList).hasSize(2);
        assertThat(first.isQualifiedForSecondRun()).isTrue();
        assertThat(second.isQualifiedForSecondRun()).isTrue();
        assertThat(third.isQualifiedForSecondRun()).isFalse();
        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.SECOND_RUN_READY);
    }

    @Test
    void calculateRankingShouldAssignRanksAndMedals() {
        Long competitionId = 1L;

        CompetitionEntity competition = createSlalomCompetition(competitionId);
        competition.setStatus(CompetitionStatus.SECOND_RUN_READY);

        SlalomResultEntity first = createRankedSlalomResult(1L, competition, createAthlete(1L), "109.531");
        SlalomResultEntity second = createRankedSlalomResult(2L, competition, createAthlete(2L), "109.900");
        SlalomResultEntity third = createRankedSlalomResult(3L, competition, createAthlete(3L), "110.800");

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(slalomResultRepository.findByCompetitionId(competitionId)).thenReturn(List.of(third, second, first));
        when(competitionRepository.save(any(CompetitionEntity.class))).thenReturn(competition);
        when(slalomResultRepository.saveAll(anyList())).thenReturn(List.of(first, second, third));
        when(slalomResultRepository.findByCompetitionIdAndQualifiedForSecondRunTrueAndTotalTimeIsNotNullOrderByTotalTimeAsc(competitionId))
                .thenReturn(List.of(first, second, third));

        List<SlalomResultResponse> ranking = slalomResultService.calculateRanking(competitionId);

        assertThat(ranking).hasSize(3);

        assertThat(first.getRankPosition()).isEqualTo(1);
        assertThat(first.getMedal()).isEqualTo(MedalType.GOLD);

        assertThat(second.getRankPosition()).isEqualTo(2);
        assertThat(second.getMedal()).isEqualTo(MedalType.SILVER);

        assertThat(third.getRankPosition()).isEqualTo(3);
        assertThat(third.getMedal()).isEqualTo(MedalType.BRONZE);

        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.RESULTS_COMPLETED);
    }

    private CompetitionEntity createSlalomCompetition(Long id) {
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

    private SlalomResultEntity createSlalomResult(
            Long id,
            CompetitionEntity competition,
            AthleteEntity athlete,
            String firstRunTime
    ) {
        SlalomResultEntity result = new SlalomResultEntity();
        result.setId(id);
        result.setCompetition(competition);
        result.setAthlete(athlete);
        result.setFirstRunTime(new BigDecimal(firstRunTime));
        result.setDidNotFinishFirstRun(false);
        result.setQualifiedForSecondRun(false);
        result.setMedal(MedalType.NONE);

        return result;
    }

    private SlalomResultEntity createRankedSlalomResult(
            Long id,
            CompetitionEntity competition,
            AthleteEntity athlete,
            String totalTime
    ) {
        SlalomResultEntity result = createSlalomResult(id, competition, athlete, "55.000");
        result.setSecondRunTime(new BigDecimal("54.000"));
        result.setTotalTime(new BigDecimal(totalTime));
        result.setQualifiedForSecondRun(true);
        result.setDidNotFinishSecondRun(false);

        return result;
    }
}