package com.example.winter_olympics.result.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import com.example.winter_olympics.registration.repository.CompetitionRegistrationRepository;
import com.example.winter_olympics.result.dto.BiathlonResultRequest;
import com.example.winter_olympics.result.dto.BiathlonResultResponse;
import com.example.winter_olympics.result.model.BiathlonResultEntity;
import com.example.winter_olympics.result.model.MedalType;
import com.example.winter_olympics.result.repository.BiathlonResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BiathlonResultServiceImpl implements BiathlonResultService {

    private static final BigDecimal PENALTY_SECONDS_PER_MISSED_SHOT = BigDecimal.valueOf(60);

    private final BiathlonResultRepository biathlonResultRepository;
    private final CompetitionRepository competitionRepository;
    private final AthleteRepository athleteRepository;
    private final CompetitionRegistrationRepository registrationRepository;

    @Override
    public BiathlonResultResponse enterResult(Long competitionId, BiathlonResultRequest request) {
        CompetitionEntity competition = findBiathlonCompetitionById(competitionId);
        AthleteEntity athlete = findAthleteById(request.athleteId());

        validateAthleteRegistration(competition.getId(), athlete.getId());

        BiathlonResultEntity result = biathlonResultRepository
                .findByCompetitionIdAndAthleteId(competition.getId(), athlete.getId())
                .orElseGet(BiathlonResultEntity::new);

        result.setCompetition(competition);
        result.setAthlete(athlete);
        result.setDidNotFinish(request.didNotFinish());

        if (request.didNotFinish()) {
            result.setSkiTime(null);
            result.setMissedShots(request.missedShots());
            result.setPenaltySeconds(null);
            result.setFinalTime(null);
            result.setRankPosition(null);
            result.setMedal(MedalType.NONE);
        } else {
            BigDecimal penaltySeconds = PENALTY_SECONDS_PER_MISSED_SHOT
                    .multiply(BigDecimal.valueOf(request.missedShots()));

            BigDecimal finalTime = request.skiTime().add(penaltySeconds);

            result.setSkiTime(request.skiTime());
            result.setMissedShots(request.missedShots());
            result.setPenaltySeconds(penaltySeconds);
            result.setFinalTime(finalTime);
            result.setRankPosition(null);
            result.setMedal(MedalType.NONE);
        }

        BiathlonResultEntity savedResult = biathlonResultRepository.save(result);

        return mapToResponse(savedResult);
    }

    @Override
    public List<BiathlonResultResponse> getResults(Long competitionId) {
        findBiathlonCompetitionById(competitionId);

        return biathlonResultRepository.findByCompetitionId(competitionId)
                .stream()
                .sorted(Comparator.comparing(
                        BiathlonResultEntity::getFinalTime,
                        Comparator.nullsLast(BigDecimal::compareTo)
                ))
                .map(this::mapToResponse)
                .toList();
    }

    private CompetitionEntity findBiathlonCompetitionById(Long competitionId) {
        CompetitionEntity competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.COMPETITION_NOT_FOUND.formatted(competitionId)
                ));

        if (competition.getType() != CompetitionType.BIATHLON) {
            throw new BadRequestException(
                    ErrorMessages.COMPETITION_IS_NOT_BIATHLON.formatted(competitionId)
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

    private BiathlonResultResponse mapToResponse(BiathlonResultEntity result) {
        AthleteEntity athlete = result.getAthlete();
        CompetitionEntity competition = result.getCompetition();

        String athleteFullName = athlete.getFirstName() + " " + athlete.getLastName();

        return new BiathlonResultResponse(
                result.getId(),
                competition.getId(),
                competition.getName(),
                athlete.getId(),
                athleteFullName,
                athlete.getCountry(),
                result.getSkiTime(),
                result.getMissedShots(),
                result.getPenaltySeconds(),
                result.getFinalTime(),
                result.isDidNotFinish(),
                result.getRankPosition(),
                result.getMedal()
        );
    }
}