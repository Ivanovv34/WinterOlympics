package com.example.winter_olympics.athlete.service;

import com.example.winter_olympics.athlete.dto.AthleteResponse;
import com.example.winter_olympics.athlete.dto.CreateAthleteRequest;
import com.example.winter_olympics.athlete.dto.UpdateAthleteRequest;
import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
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
class AthleteServiceImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private AthleteServiceImpl athleteService;

    @Test
    void getAllAthletesShouldReturnAllAthletesSortedById() {
        AthleteEntity athleteTwo = createAthlete(2L, "Henrik", "Kristoffersen", "Norway");
        AthleteEntity athleteOne = createAthlete(1L, "Marco", "Odermatt", "Switzerland");

        when(athleteRepository.findAll()).thenReturn(List.of(athleteTwo, athleteOne));

        List<AthleteResponse> response = athleteService.getAllAthletes();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(0).firstName()).isEqualTo("Marco");
        assertThat(response.get(1).id()).isEqualTo(2L);
        assertThat(response.get(1).firstName()).isEqualTo("Henrik");
    }

    @Test
    void getAthleteByIdShouldReturnAthleteWhenItExists() {
        AthleteEntity athlete = createAthlete(1L, "Marco", "Odermatt", "Switzerland");

        when(athleteRepository.findById(1L)).thenReturn(Optional.of(athlete));

        AthleteResponse response = athleteService.getAthleteById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Marco");
        assertThat(response.lastName()).isEqualTo("Odermatt");
        assertThat(response.country()).isEqualTo("Switzerland");
        assertThat(response.gender()).isEqualTo(Gender.MALE);
    }

    @Test
    void getAthleteByIdShouldThrowWhenAthleteDoesNotExist() {
        when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.getAthleteById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Athlete with id 99 was not found");
    }

    @Test
    void createAthleteShouldSaveAndReturnAthlete() {
        CreateAthleteRequest request = new CreateAthleteRequest(
                "Marco",
                "Odermatt",
                "Switzerland",
                Gender.MALE,
                LocalDate.of(1997, 10, 8)
        );

        AthleteEntity savedAthlete = createAthlete(1L, "Marco", "Odermatt", "Switzerland");

        when(athleteRepository.save(any(AthleteEntity.class))).thenReturn(savedAthlete);

        AthleteResponse response = athleteService.createAthlete(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Marco");
        assertThat(response.lastName()).isEqualTo("Odermatt");
        assertThat(response.country()).isEqualTo("Switzerland");

        verify(athleteRepository).save(any(AthleteEntity.class));
    }

    @Test
    void updateAthleteShouldUpdateExistingAthlete() {
        AthleteEntity existingAthlete = createAthlete(1L, "Marco", "Odermatt", "Switzerland");

        UpdateAthleteRequest request = new UpdateAthleteRequest(
                "Marcel",
                "Hirscher",
                "Austria",
                Gender.MALE,
                LocalDate.of(1989, 3, 2)
        );

        AthleteEntity updatedAthlete = createAthlete(1L, "Marcel", "Hirscher", "Austria");
        updatedAthlete.setBirthDate(LocalDate.of(1989, 3, 2));

        when(athleteRepository.findById(1L)).thenReturn(Optional.of(existingAthlete));
        when(athleteRepository.save(any(AthleteEntity.class))).thenReturn(updatedAthlete);

        AthleteResponse response = athleteService.updateAthlete(1L, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Marcel");
        assertThat(response.lastName()).isEqualTo("Hirscher");
        assertThat(response.country()).isEqualTo("Austria");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1989, 3, 2));

        verify(athleteRepository).save(existingAthlete);
    }

    @Test
    void updateAthleteShouldThrowWhenAthleteDoesNotExist() {
        UpdateAthleteRequest request = new UpdateAthleteRequest(
                "Marcel",
                "Hirscher",
                "Austria",
                Gender.MALE,
                LocalDate.of(1989, 3, 2)
        );

        when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.updateAthlete(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Athlete with id 99 was not found");

        verify(athleteRepository, never()).save(any(AthleteEntity.class));
    }

    @Test
    void deleteAthleteShouldDeleteExistingAthlete() {
        AthleteEntity athlete = createAthlete(1L, "Marco", "Odermatt", "Switzerland");

        when(athleteRepository.findById(1L)).thenReturn(Optional.of(athlete));

        athleteService.deleteAthlete(1L);

        verify(athleteRepository).delete(athlete);
    }

    @Test
    void deleteAthleteShouldThrowWhenAthleteDoesNotExist() {
        when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.deleteAthlete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Athlete with id 99 was not found");

        verify(athleteRepository, never()).delete(any(AthleteEntity.class));
    }

    private AthleteEntity createAthlete(Long id, String firstName, String lastName, String country) {
        AthleteEntity athlete = new AthleteEntity();
        athlete.setId(id);
        athlete.setFirstName(firstName);
        athlete.setLastName(lastName);
        athlete.setCountry(country);
        athlete.setGender(Gender.MALE);
        athlete.setBirthDate(LocalDate.of(1997, 10, 8));

        return athlete;
    }
}