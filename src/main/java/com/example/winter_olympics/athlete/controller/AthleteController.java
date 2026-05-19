package com.example.winter_olympics.athlete.controller;

import com.example.winter_olympics.athlete.dto.AthleteResponse;
import com.example.winter_olympics.athlete.dto.CreateAthleteRequest;
import com.example.winter_olympics.athlete.dto.UpdateAthleteRequest;
import com.example.winter_olympics.athlete.service.AthleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/athletes")
@SecurityRequirement(name = "bearerAuth")
public class AthleteController {

    private final AthleteService athleteService;

    @GetMapping
    public ResponseEntity<List<AthleteResponse>> getAllAthletes() {
        return ResponseEntity.ok(athleteService.getAllAthletes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AthleteResponse> getAthleteById(@PathVariable Long id) {
        return ResponseEntity.ok(athleteService.getAthleteById(id));
    }

    @PostMapping
    public ResponseEntity<AthleteResponse> createAthlete(@Valid @RequestBody CreateAthleteRequest request) {
        AthleteResponse createdAthlete = athleteService.createAthlete(request);

        URI location = URI.create("/api/athletes/" + createdAthlete.id());

        return ResponseEntity.created(location).body(createdAthlete);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AthleteResponse> updateAthlete(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAthleteRequest request
    ) {
        return ResponseEntity.ok(athleteService.updateAthlete(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAthlete(@PathVariable Long id) {
        athleteService.deleteAthlete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}