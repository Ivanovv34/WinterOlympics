package com.example.winter_olympics.competition.controller;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.competition.dto.CompetitionResponse;
import com.example.winter_olympics.competition.dto.CreateCompetitionRequest;
import com.example.winter_olympics.competition.dto.UpdateCompetitionRequest;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.competition.service.CompetitionService;
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
@RequestMapping("/api/competitions")
@SecurityRequirement(name = "basicAuth")
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping
    public ResponseEntity<List<CompetitionResponse>> getAllCompetitions(
            @RequestParam(required = false) CompetitionType type,
            @RequestParam(required = false) Gender gender
    ) {
        return ResponseEntity.ok(competitionService.getAllCompetitions(type, gender));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getCompetitionById(@PathVariable Long id) {
        return ResponseEntity.ok(competitionService.getCompetitionById(id));
    }

    @PostMapping
    public ResponseEntity<CompetitionResponse> createCompetition(
            @Valid @RequestBody CreateCompetitionRequest request
    ) {
        CompetitionResponse createdCompetition = competitionService.createCompetition(request);

        URI location = URI.create("/api/competitions/" + createdCompetition.id());

        return ResponseEntity.created(location).body(createdCompetition);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionResponse> updateCompetition(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompetitionRequest request
    ) {
        return ResponseEntity.ok(competitionService.updateCompetition(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetition(@PathVariable Long id) {
        competitionService.deleteCompetition(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}