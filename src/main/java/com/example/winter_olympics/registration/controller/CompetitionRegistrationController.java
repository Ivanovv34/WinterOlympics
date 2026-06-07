package com.example.winter_olympics.registration.controller;

import com.example.winter_olympics.registration.dto.RegistrationResponse;
import com.example.winter_olympics.registration.service.CompetitionRegistrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CompetitionRegistrationController {

    private final CompetitionRegistrationService registrationService;

    // ── Register by athleteId (ADMIN) ─────────────────────────────────────
    @PostMapping("/api/competitions/{competitionId}/registrations/{athleteId}")
    public ResponseEntity<RegistrationResponse> registerAthlete(
            @PathVariable Long competitionId,
            @PathVariable Long athleteId
    ) {
        RegistrationResponse registration = registrationService.registerAthlete(competitionId, athleteId);

        URI location = URI.create(
                "/api/competitions/" + competitionId + "/registrations/" + athleteId
        );

        return ResponseEntity.created(location).body(registration);
    }

    // ── Register current user's athlete (ATHLETE) ─────────────────────────
    @PostMapping("/api/competitions/{competitionId}/registrations/me")
    public ResponseEntity<RegistrationResponse> registerMe(
            @PathVariable Long competitionId
    ) {
        RegistrationResponse registration = registrationService.registerMe(competitionId);

        URI location = URI.create(
                "/api/competitions/" + competitionId + "/registrations/me"
        );

        return ResponseEntity.created(location).body(registration);
    }

    // ── Unregister by athleteId (ADMIN) ───────────────────────────────────
    @DeleteMapping("/api/competitions/{competitionId}/registrations/{athleteId}")
    public ResponseEntity<Void> unregisterAthlete(
            @PathVariable Long competitionId,
            @PathVariable Long athleteId
    ) {
        registrationService.unregisterAthlete(competitionId, athleteId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ── Unregister current user's athlete (ATHLETE) ───────────────────────
    @DeleteMapping("/api/competitions/{competitionId}/registrations/me")
    public ResponseEntity<Void> unregisterMe(
            @PathVariable Long competitionId
    ) {
        registrationService.unregisterMe(competitionId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/api/competitions/{competitionId}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByCompetition(
            @PathVariable Long competitionId
    ) {
        return ResponseEntity.ok(
                registrationService.getRegistrationsByCompetition(competitionId)
        );
    }

    @GetMapping("/api/athletes/{athleteId}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByAthlete(
            @PathVariable Long athleteId
    ) {
        return ResponseEntity.ok(
                registrationService.getRegistrationsByAthlete(athleteId)
        );
    }
}