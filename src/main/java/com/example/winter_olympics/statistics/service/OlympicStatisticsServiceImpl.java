package com.example.winter_olympics.statistics.service;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.model.CompetitionEntity;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OlympicStatisticsServiceImpl implements OlympicStatisticsService {

    private final SlalomResultRepository slalomResultRepository;
    private final BiathlonResultRepository biathlonResultRepository;
    private final CompetitionRegistrationRepository registrationRepository;

    @Override
    public List<CountryMedalResponse> getMedalsByCountry() {
        Map<String, MedalCounter> medalsByCountry = new LinkedHashMap<>();

        slalomResultRepository.findAll()
                .stream()
                .filter(this::hasMedal)
                .forEach(result -> addMedal(
                        medalsByCountry,
                        result.getAthlete().getCountry(),
                        result.getMedal()
                ));

        biathlonResultRepository.findAll()
                .stream()
                .filter(this::hasMedal)
                .forEach(result -> addMedal(
                        medalsByCountry,
                        result.getAthlete().getCountry(),
                        result.getMedal()
                ));

        return medalsByCountry.entrySet()
                .stream()
                .map(entry -> new CountryMedalResponse(
                        entry.getKey(),
                        entry.getValue().goldMedals,
                        entry.getValue().silverMedals,
                        entry.getValue().bronzeMedals,
                        entry.getValue().getTotalMedals()
                ))
                .sorted(Comparator.comparing(CountryMedalResponse::totalMedals).reversed()
                        .thenComparing(CountryMedalResponse::goldMedals, Comparator.reverseOrder())
                        .thenComparing(CountryMedalResponse::silverMedals, Comparator.reverseOrder())
                        .thenComparing(CountryMedalResponse::bronzeMedals, Comparator.reverseOrder())
                        .thenComparing(CountryMedalResponse::country))
                .toList();
    }

    @Override
    public AverageAgeResponse getAverageAge() {
        List<AthleteEntity> participants = registrationRepository.findAll()
                .stream()
                .map(CompetitionRegistrationEntity::getAthlete)
                .distinct()
                .toList();

        if (participants.isEmpty()) {
            return new AverageAgeResponse(0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        LocalDate today = LocalDate.now();

        BigDecimal totalAge = participants.stream()
                .map(athlete -> calculateAge(athlete.getBirthDate(), today))
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAge = totalAge.divide(
                BigDecimal.valueOf(participants.size()),
                2,
                RoundingMode.HALF_UP
        );

        return new AverageAgeResponse(participants.size(), averageAge);
    }

    @Override
    public MedalistResponse getYoungestMedalist() {
        return getAllMedalists()
                .stream()
                .min(Comparator.comparing(MedalistResponse::age))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.NO_MEDALISTS_FOUND));
    }

    @Override
    public MedalistResponse getOldestMedalist() {
        return getAllMedalists()
                .stream()
                .max(Comparator.comparing(MedalistResponse::age))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.NO_MEDALISTS_FOUND));
    }

    private List<MedalistResponse> getAllMedalists() {
        List<MedalistResponse> medalists = new ArrayList<>();

        slalomResultRepository.findAll()
                .stream()
                .filter(this::hasMedal)
                .map(result -> mapToMedalistResponse(
                        result.getAthlete(),
                        result.getCompetition(),
                        result.getRankPosition(),
                        result.getMedal()
                ))
                .forEach(medalists::add);

        biathlonResultRepository.findAll()
                .stream()
                .filter(this::hasMedal)
                .map(result -> mapToMedalistResponse(
                        result.getAthlete(),
                        result.getCompetition(),
                        result.getRankPosition(),
                        result.getMedal()
                ))
                .forEach(medalists::add);

        return medalists;
    }

    private boolean hasMedal(SlalomResultEntity result) {
        return result.getMedal() != null && result.getMedal() != MedalType.NONE;
    }

    private boolean hasMedal(BiathlonResultEntity result) {
        return result.getMedal() != null && result.getMedal() != MedalType.NONE;
    }

    private void addMedal(Map<String, MedalCounter> medalsByCountry, String country, MedalType medal) {
        MedalCounter counter = medalsByCountry.computeIfAbsent(country, key -> new MedalCounter());

        switch (medal) {
            case GOLD -> counter.goldMedals++;
            case SILVER -> counter.silverMedals++;
            case BRONZE -> counter.bronzeMedals++;
            case NONE -> {
            }
        }
    }

    private MedalistResponse mapToMedalistResponse(
            AthleteEntity athlete,
            CompetitionEntity competition,
            Integer rankPosition,
            MedalType medal
    ) {
        LocalDate today = LocalDate.now();

        return new MedalistResponse(
                athlete.getId(),
                athlete.getFirstName() + " " + athlete.getLastName(),
                athlete.getCountry(),
                athlete.getBirthDate(),
                calculateAge(athlete.getBirthDate(), today),
                competition.getId(),
                competition.getName(),
                competition.getType(),
                rankPosition,
                medal
        );
    }

    private int calculateAge(LocalDate birthDate, LocalDate date) {
        return Period.between(birthDate, date).getYears();
    }

    private static class MedalCounter {

        private int goldMedals;
        private int silverMedals;
        private int bronzeMedals;

        private int getTotalMedals() {
            return goldMedals + silverMedals + bronzeMedals;
        }
    }
}