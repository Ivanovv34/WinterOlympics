package com.example.winter_olympics.competition.service;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.dto.CompetitionResponse;
import com.example.winter_olympics.competition.dto.CreateCompetitionRequest;
import com.example.winter_olympics.competition.dto.UpdateCompetitionRequest;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;

    @Override
    public List<CompetitionResponse> getAllCompetitions(CompetitionType type, Gender gender) {
        List<CompetitionEntity> competitions;

        if (type != null && gender != null) {
            competitions = competitionRepository.findByTypeAndGender(type, gender);
        } else if (type != null) {
            competitions = competitionRepository.findByType(type);
        } else if (gender != null) {
            competitions = competitionRepository.findByGender(gender);
        } else {
            competitions = competitionRepository.findAll();
        }

        return competitions.stream()
                .sorted(Comparator.comparing(CompetitionEntity::getCompetitionDate)
                        .thenComparing(CompetitionEntity::getId))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CompetitionResponse getCompetitionById(Long id) {
        CompetitionEntity competition = findCompetitionById(id);

        return mapToResponse(competition);
    }

    @Override
    public CompetitionResponse createCompetition(CreateCompetitionRequest request) {
        CompetitionEntity competition = new CompetitionEntity();

        competition.setName(request.name());
        competition.setType(request.type());
        competition.setGender(request.gender());
        competition.setMinAge(request.minAge());
        competition.setCompetitionDate(request.competitionDate());
        competition.setStatus(CompetitionStatus.OPEN);

        CompetitionEntity savedCompetition = competitionRepository.save(competition);

        return mapToResponse(savedCompetition);
    }

    @Override
    public CompetitionResponse updateCompetition(Long id, UpdateCompetitionRequest request) {
        CompetitionEntity competition = findCompetitionById(id);

        competition.setName(request.name());
        competition.setType(request.type());
        competition.setGender(request.gender());
        competition.setMinAge(request.minAge());
        competition.setCompetitionDate(request.competitionDate());
        competition.setStatus(request.status());

        CompetitionEntity updatedCompetition = competitionRepository.save(competition);

        return mapToResponse(updatedCompetition);
    }

    @Override
    public void deleteCompetition(Long id) {
        CompetitionEntity competition = findCompetitionById(id);

        competitionRepository.delete(competition);
    }

    private CompetitionEntity findCompetitionById(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.COMPETITION_NOT_FOUND.formatted(id)
                ));
    }

    private CompetitionResponse mapToResponse(CompetitionEntity competition) {
        return new CompetitionResponse(
                competition.getId(),
                competition.getName(),
                competition.getType(),
                competition.getGender(),
                competition.getMinAge(),
                competition.getCompetitionDate(),
                competition.getStatus()
        );
    }
}