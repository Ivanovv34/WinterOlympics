package com.example.winter_olympics.competition.service;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.competition.dto.CompetitionResponse;
import com.example.winter_olympics.competition.dto.CreateCompetitionRequest;
import com.example.winter_olympics.competition.dto.UpdateCompetitionRequest;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import com.example.winter_olympics.competition.model.CompetitionStatus;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.repository.CompetitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @InjectMocks
    private CompetitionServiceImpl competitionService;

    @Test
    void getAllCompetitionsShouldReturnAllCompetitionsSortedByDateAndId() {
        CompetitionEntity laterCompetition = createCompetition(
                2L,
                "Women Biathlon 2026",
                CompetitionType.BIATHLON,
                Gender.FEMALE,
                LocalDate.of(2026, 6, 5)
        );

        CompetitionEntity earlierCompetition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.findAll()).thenReturn(List.of(laterCompetition, earlierCompetition));

        List<CompetitionResponse> response = competitionService.getAllCompetitions(null, null);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(0).name()).isEqualTo("Men Slalom 2026");
        assertThat(response.get(1).id()).isEqualTo(2L);
        assertThat(response.get(1).name()).isEqualTo("Women Biathlon 2026");
    }

    @Test
    void getAllCompetitionsShouldFilterByType() {
        CompetitionEntity competition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.findByType(CompetitionType.SLALOM)).thenReturn(List.of(competition));

        List<CompetitionResponse> response = competitionService.getAllCompetitions(CompetitionType.SLALOM, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).type()).isEqualTo(CompetitionType.SLALOM);

        verify(competitionRepository).findByType(CompetitionType.SLALOM);
        verify(competitionRepository, never()).findAll();
    }

    @Test
    void getAllCompetitionsShouldFilterByGender() {
        CompetitionEntity competition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.findByGender(Gender.MALE)).thenReturn(List.of(competition));

        List<CompetitionResponse> response = competitionService.getAllCompetitions(null, Gender.MALE);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).gender()).isEqualTo(Gender.MALE);

        verify(competitionRepository).findByGender(Gender.MALE);
        verify(competitionRepository, never()).findAll();
    }

    @Test
    void getAllCompetitionsShouldFilterByTypeAndGender() {
        CompetitionEntity competition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.findByTypeAndGender(CompetitionType.SLALOM, Gender.MALE))
                .thenReturn(List.of(competition));

        List<CompetitionResponse> response = competitionService.getAllCompetitions(
                CompetitionType.SLALOM,
                Gender.MALE
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).type()).isEqualTo(CompetitionType.SLALOM);
        assertThat(response.get(0).gender()).isEqualTo(Gender.MALE);

        verify(competitionRepository).findByTypeAndGender(CompetitionType.SLALOM, Gender.MALE);
        verify(competitionRepository, never()).findAll();
    }

    @Test
    void getCompetitionByIdShouldReturnCompetitionWhenItExists() {
        CompetitionEntity competition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        CompetitionResponse response = competitionService.getCompetitionById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Men Slalom 2026");
        assertThat(response.type()).isEqualTo(CompetitionType.SLALOM);
        assertThat(response.gender()).isEqualTo(Gender.MALE);
    }

    @Test
    void getCompetitionByIdShouldThrowWhenCompetitionDoesNotExist() {
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.getCompetitionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Competition with id 99 was not found");
    }

    @Test
    void createCompetitionShouldSaveCompetitionWithOpenStatus() {
        CreateCompetitionRequest request = new CreateCompetitionRequest(
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                18,
                LocalDate.of(2026, 6, 1)
        );

        CompetitionEntity savedCompetition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.save(any(CompetitionEntity.class))).thenReturn(savedCompetition);

        CompetitionResponse response = competitionService.createCompetition(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Men Slalom 2026");
        assertThat(response.status()).isEqualTo(CompetitionStatus.OPEN);

        verify(competitionRepository).save(any(CompetitionEntity.class));
    }

    @Test
    void updateCompetitionShouldUpdateExistingCompetition() {
        CompetitionEntity existingCompetition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        UpdateCompetitionRequest request = new UpdateCompetitionRequest(
                "Men Slalom Final 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                21,
                LocalDate.of(2026, 6, 2),
                CompetitionStatus.SECOND_RUN_READY
        );

        CompetitionEntity updatedCompetition = createCompetition(
                1L,
                "Men Slalom Final 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 2)
        );
        updatedCompetition.setMinAge(21);
        updatedCompetition.setStatus(CompetitionStatus.SECOND_RUN_READY);

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(existingCompetition));
        when(competitionRepository.save(any(CompetitionEntity.class))).thenReturn(updatedCompetition);

        CompetitionResponse response = competitionService.updateCompetition(1L, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Men Slalom Final 2026");
        assertThat(response.minAge()).isEqualTo(21);
        assertThat(response.competitionDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(response.status()).isEqualTo(CompetitionStatus.SECOND_RUN_READY);

        verify(competitionRepository).save(existingCompetition);
    }

    @Test
    void updateCompetitionShouldThrowWhenCompetitionDoesNotExist() {
        UpdateCompetitionRequest request = new UpdateCompetitionRequest(
                "Men Slalom Final 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                21,
                LocalDate.of(2026, 6, 2),
                CompetitionStatus.SECOND_RUN_READY
        );

        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.updateCompetition(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Competition with id 99 was not found");

        verify(competitionRepository, never()).save(any(CompetitionEntity.class));
    }

    @Test
    void deleteCompetitionShouldDeleteExistingCompetition() {
        CompetitionEntity competition = createCompetition(
                1L,
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                LocalDate.of(2026, 6, 1)
        );

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        competitionService.deleteCompetition(1L);

        verify(competitionRepository).delete(competition);
    }

    @Test
    void deleteCompetitionShouldThrowWhenCompetitionDoesNotExist() {
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.deleteCompetition(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Competition with id 99 was not found");

        verify(competitionRepository, never()).delete(any(CompetitionEntity.class));
    }

    private CompetitionEntity createCompetition(
            Long id,
            String name,
            CompetitionType type,
            Gender gender,
            LocalDate competitionDate
    ) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setId(id);
        competition.setName(name);
        competition.setType(type);
        competition.setGender(gender);
        competition.setMinAge(18);
        competition.setCompetitionDate(competitionDate);
        competition.setStatus(CompetitionStatus.OPEN);

        return competition;
    }
}