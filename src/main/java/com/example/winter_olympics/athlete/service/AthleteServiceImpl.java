package com.example.winter_olympics.athlete.service;

import com.example.winter_olympics.athlete.dto.AthleteResponse;
import com.example.winter_olympics.athlete.dto.CreateAthleteRequest;
import com.example.winter_olympics.athlete.dto.UpdateAthleteRequest;
import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.athlete.repository.AthleteRepository;
import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.common.exception.ResourceNotFoundException;
import com.example.winter_olympics.user.model.UserEntity;
import com.example.winter_olympics.user.model.Role;
import com.example.winter_olympics.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AthleteServiceImpl implements AthleteService {

    private final AthleteRepository athleteRepository;
    private final UserRepository userRepository;

    @Override
    public List<AthleteResponse> getAllAthletes() {
        return athleteRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(AthleteEntity::getId))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AthleteResponse getAthleteById(Long id) {
        AthleteEntity athlete = findAthleteById(id);
        return mapToResponse(athlete);
    }

    @Override
    public AthleteResponse createAthlete(CreateAthleteRequest request) {
        AthleteEntity athlete = new AthleteEntity();

        athlete.setFirstName(request.firstName());
        athlete.setLastName(request.lastName());
        athlete.setCountry(request.country());
        athlete.setGender(request.gender());
        athlete.setBirthDate(request.birthDate());

        // Link to current user if they are an ATHLETE
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getRole() == Role.ATHLETE) {
                if (athleteRepository.existsByUserId(user.getId())) {
                    throw new BadRequestException(
                            "You have already registered an athlete profile. Each account can only have one athlete profile."
                    );
                }
                athlete.setUser(user);
            }
        });

        AthleteEntity savedAthlete = athleteRepository.save(athlete);
        return mapToResponse(savedAthlete);
    }

    @Override
    public AthleteResponse updateAthlete(Long id, UpdateAthleteRequest request) {
        AthleteEntity athlete = findAthleteById(id);

        // Check permission: ATHLETE can only update their own profile
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getRole() == Role.ATHLETE) {
                if (athlete.getUser() == null || !athlete.getUser().getId().equals(user.getId())) {
                    throw new BadRequestException("You can only edit your own athlete profile.");
                }
            }
        });

        athlete.setFirstName(request.firstName());
        athlete.setLastName(request.lastName());
        athlete.setCountry(request.country());
        athlete.setGender(request.gender());
        athlete.setBirthDate(request.birthDate());

        AthleteEntity updatedAthlete = athleteRepository.save(athlete);
        return mapToResponse(updatedAthlete);
    }

    @Override
    public void deleteAthlete(Long id) {
        AthleteEntity athlete = findAthleteById(id);

        // Check permission: ATHLETE can only delete their own profile
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getRole() == Role.ATHLETE) {
                if (athlete.getUser() == null || !athlete.getUser().getId().equals(user.getId())) {
                    throw new BadRequestException("You can only delete your own athlete profile.");
                }
            }
        });

        athleteRepository.delete(athlete);
    }

    private AthleteEntity findAthleteById(Long id) {
        return athleteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.ATHLETE_NOT_FOUND.formatted(id)
                ));
    }

    private AthleteResponse mapToResponse(AthleteEntity athlete) {
        Long userId = athlete.getUser() != null
                ? athlete.getUser().getId()
                : null;

        return new AthleteResponse(
                athlete.getId(),
                userId,
                athlete.getFirstName(),
                athlete.getLastName(),
                athlete.getCountry(),
                athlete.getGender(),
                athlete.getBirthDate()
        );
    }
}