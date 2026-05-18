package com.example.winter_olympics.result.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import com.example.winter_olympics.registration.repository.CompetitionRegistrationRepository;
import com.example.winter_olympics.result.dto.SlalomFirstRunRequest;
import com.example.winter_olympics.result.dto.SlalomQualificationRequest;
import com.example.winter_olympics.result.dto.SlalomResultResponse;
import com.example.winter_olympics.result.dto.SlalomSecondRunRequest;
import com.example.winter_olympics.result.model.MedalType;
import com.example.winter_olympics.result.model.SlalomResultEntity;
import com.example.winter_olympics.result.repository.SlalomResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlalomResultServiceImpl implements SlalomResultService {

    private final SlalomResultRepository slalomResultRepository;
    private final CompetitionRepository competitionRepository;
    private final AthleteRepository athleteRepository;
    private final CompetitionRegistrationRepository registrationRepository;

    @Override
    public SlalomResultResponse enterFirstRunResult(Long competitionId, SlalomFirstRunRequest request) {
        CompetitionEntity competition = findSlalomCompetitionById(competitionId);
        AthleteEntity athlete = findAthleteById(request.athleteId());

        validateAthleteRegistration(competition.getId(), athlete.getId());

        SlalomResultEntity result = slalomResultRepository
                .findByCompetitionIdAndAthleteId(competition.getId(), athlete.getId())
                .orElseGet(SlalomResultEntity::new);

        result.setCompetition(competition);
        result.setAthlete(athlete);
        result.setDidNotFinishFirstRun(request.didNotFinish());

        if (request.didNotFinish()) {
            result.setFirstRunTime(null);
            result.setQualifiedForSecondRun(false);
            result.setSecondRunTime(null);
            result.setTotalTime(null);
            result.setRankPosition(null);
            result.setMedal(MedalType.NONE);
        } else {
            result.setFirstRunTime(request.firstRunTime());
        }

        SlalomResultEntity savedResult = slalomResultRepository.save(result);

        return mapToResponse(savedResult);
    }

    @Override
    public List<SlalomResultResponse> qualifyForSecondRun(Long competitionId, SlalomQualificationRequest request) {
        CompetitionEntity competition = findSlalomCompetitionById(competitionId);

        List<SlalomResultEntity> allResults = slalomResultRepository.findByCompetitionId(competition.getId());

        allResults.forEach(result -> result.setQualifiedForSecondRun(false));

        List<SlalomResultEntity> qualifiedResults = allResults.stream()
                .filter(result -> !result.isDidNotFinishFirstRun())
                .filter(result -> result.getFirstRunTime() != null)
                .sorted(Comparator.comparing(SlalomResultEntity::getFirstRunTime))
                .limit(request.qualificationLimit())
                .toList();

        qualifiedResults.forEach(result -> result.setQualifiedForSecondRun(true));

        competition.setStatus(CompetitionStatus.SECOND_RUN_READY);
        competitionRepository.save(competition);

        slalomResultRepository.saveAll(allResults);

        return slalomResultRepository
                .findByCompetitionIdAndQualifiedForSecondRunTrueOrderByFirstRunTimeDesc(competition.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SlalomResultResponse enterSecondRunResult(Long competitionId, SlalomSecondRunRequest request) {
        CompetitionEntity competition = findSlalomCompetitionById(competitionId);

        if (competition.getStatus() != CompetitionStatus.SECOND_RUN_READY) {
            throw new BadRequestException(ErrorMessages.SECOND_RUN_CANNOT_BE_ENTERED_BEFORE_QUALIFICATION);
        }

        SlalomResultEntity result = slalomResultRepository
                .findByCompetitionIdAndAthleteId(competition.getId(), request.athleteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.SLALOM_RESULT_NOT_FOUND.formatted(competition.getId(), request.athleteId())
                ));

        if (!result.isQualifiedForSecondRun()) {
            throw new BadRequestException(
                    ErrorMessages.ATHLETE_NOT_QUALIFIED_FOR_SECOND_RUN.formatted(request.athleteId())
            );
        }

        result.setDidNotFinishSecondRun(request.didNotFinish());

        if (request.didNotFinish()) {
            result.setSecondRunTime(null);
            result.setTotalTime(null);
            result.setRankPosition(null);
            result.setMedal(MedalType.NONE);
        } else {
            result.setSecondRunTime(request.secondRunTime());
            result.setTotalTime(result.getFirstRunTime().add(request.secondRunTime()));
        }

        SlalomResultEntity savedResult = slalomResultRepository.save(result);

        return mapToResponse(savedResult);
    }

    @Override
    public List<SlalomResultResponse> getSlalomResults(Long competitionId) {
        findSlalomCompetitionById(competitionId);

        return slalomResultRepository.findByCompetitionId(competitionId)
                .stream()
                .sorted(Comparator.comparing(
                        SlalomResultEntity::getFirstRunTime,
                        Comparator.nullsLast(BigDecimal::compareTo)
                ))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SlalomResultResponse> getSecondRunStartList(Long competitionId) {
        findSlalomCompetitionById(competitionId);

        return slalomResultRepository
                .findByCompetitionIdAndQualifiedForSecondRunTrueOrderByFirstRunTimeDesc(competitionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SlalomResultResponse> calculateRanking(Long competitionId) {
        CompetitionEntity competition = findSlalomCompetitionById(competitionId);

        if (competition.getStatus() != CompetitionStatus.SECOND_RUN_READY
                && competition.getStatus() != CompetitionStatus.RESULTS_COMPLETED) {
            throw new BadRequestException(ErrorMessages.SLALOM_RANKING_CANNOT_BE_CALCULATED_BEFORE_SECOND_RUN);
        }

        List<SlalomResultEntity> allResults = slalomResultRepository.findByCompetitionId(competitionId);

        allResults.forEach(result -> {
            result.setRankPosition(null);
            result.setMedal(MedalType.NONE);
        });

        List<SlalomResultEntity> rankedResults = allResults.stream()
                .filter(SlalomResultEntity::isQualifiedForSecondRun)
                .filter(result -> !result.isDidNotFinishFirstRun())
                .filter(result -> !result.isDidNotFinishSecondRun())
                .filter(result -> result.getTotalTime() != null)
                .sorted(Comparator.comparing(SlalomResultEntity::getTotalTime))
                .toList();

        if (rankedResults.isEmpty()) {
            throw new BadRequestException(ErrorMessages.NO_VALID_SLALOM_RESULTS_FOR_RANKING);
        }

        for (int i = 0; i < rankedResults.size(); i++) {
            SlalomResultEntity result = rankedResults.get(i);
            int rank = i + 1;

            result.setRankPosition(rank);
            result.setMedal(getMedalByRank(rank));
        }

        competition.setStatus(CompetitionStatus.RESULTS_COMPLETED);
        competitionRepository.save(competition);

        slalomResultRepository.saveAll(allResults);

        return getRanking(competitionId);
    }

    @Override
    public List<SlalomResultResponse> getRanking(Long competitionId) {
        findSlalomCompetitionById(competitionId);

        return slalomResultRepository
                .findByCompetitionIdAndQualifiedForSecondRunTrueAndTotalTimeIsNotNullOrderByTotalTimeAsc(competitionId)
                .stream()
                .filter(result -> !result.isDidNotFinishFirstRun())
                .filter(result -> !result.isDidNotFinishSecondRun())
                .map(this::mapToResponse)
                .toList();
    }

    private MedalType getMedalByRank(int rank) {
        return switch (rank) {
            case 1 -> MedalType.GOLD;
            case 2 -> MedalType.SILVER;
            case 3 -> MedalType.BRONZE;
            default -> MedalType.NONE;
        };
    }

    private CompetitionEntity findSlalomCompetitionById(Long competitionId) {
        CompetitionEntity competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.COMPETITION_NOT_FOUND.formatted(competitionId)
                ));

        if (competition.getType() != CompetitionType.SLALOM) {
            throw new BadRequestException(
                    ErrorMessages.COMPETITION_IS_NOT_SLALOM.formatted(competitionId)
            );
        }

        return competition;
    }

    private AthleteEntity findAthleteById(Long athleteId) {
        return athleteRepository.findById(athleteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.ATHLETE_NOT_FOUND.formatted(athleteId)
                ));
    }

    private void validateAthleteRegistration(Long competitionId, Long athleteId) {
        boolean isRegistered = registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId);

        if (!isRegistered) {
            throw new BadRequestException(
                    ErrorMessages.ATHLETE_NOT_REGISTERED_FOR_COMPETITION.formatted(athleteId, competitionId)
            );
        }
    }

    private SlalomResultResponse mapToResponse(SlalomResultEntity result) {
        AthleteEntity athlete = result.getAthlete();
        CompetitionEntity competition = result.getCompetition();

        String athleteFullName = athlete.getFirstName() + " " + athlete.getLastName();

        return new SlalomResultResponse(
                result.getId(),
                competition.getId(),
                competition.getName(),
                athlete.getId(),
                athleteFullName,
                athlete.getCountry(),
                result.getFirstRunTime(),
                result.getSecondRunTime(),
                result.isDidNotFinishFirstRun(),
                result.isDidNotFinishSecondRun(),
                result.isQualifiedForSecondRun(),
                result.getTotalTime(),
                result.getRankPosition(),
                result.getMedal()
        );
    }
}