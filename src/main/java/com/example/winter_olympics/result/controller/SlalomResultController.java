package com.example.winter_olympics.result.controller;

import com.example.winter_olympics.result.dto.SlalomFirstRunRequest;
import com.example.winter_olympics.result.dto.SlalomQualificationRequest;
import com.example.winter_olympics.result.dto.SlalomResultResponse;
import com.example.winter_olympics.result.dto.SlalomSecondRunRequest;
import com.example.winter_olympics.result.service.SlalomResultService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/competitions/{competitionId}/slalom")
@SecurityRequirement(name = "bearerAuth")
public class SlalomResultController {

    private final SlalomResultService slalomResultService;

    @PostMapping("/first-run")
    public ResponseEntity<SlalomResultResponse> enterFirstRunResult(
            @PathVariable Long competitionId,
            @Valid @RequestBody SlalomFirstRunRequest request
    ) {
        return ResponseEntity.ok(
                slalomResultService.enterFirstRunResult(competitionId, request)
        );
    }

    @PostMapping("/qualify-second-run")
    public ResponseEntity<List<SlalomResultResponse>> qualifyForSecondRun(
            @PathVariable Long competitionId,
            @Valid @RequestBody SlalomQualificationRequest request
    ) {
        return ResponseEntity.ok(
                slalomResultService.qualifyForSecondRun(competitionId, request)
        );
    }

    @PostMapping("/second-run")
    public ResponseEntity<SlalomResultResponse> enterSecondRunResult(
            @PathVariable Long competitionId,
            @Valid @RequestBody SlalomSecondRunRequest request
    ) {
        return ResponseEntity.ok(
                slalomResultService.enterSecondRunResult(competitionId, request)
        );
    }

    @PostMapping("/calculate-ranking")
    public ResponseEntity<List<SlalomResultResponse>> calculateRanking(
            @PathVariable Long competitionId
    ) {
        return ResponseEntity.ok(
                slalomResultService.calculateRanking(competitionId)
        );
    }

    @GetMapping("/results")
    public ResponseEntity<List<SlalomResultResponse>> getSlalomResults(
            @PathVariable Long competitionId
    ) {
        return ResponseEntity.ok(
                slalomResultService.getSlalomResults(competitionId)
        );
    }

    @GetMapping("/second-run-start-list")
    public ResponseEntity<List<SlalomResultResponse>> getSecondRunStartList(
            @PathVariable Long competitionId
    ) {
        return ResponseEntity.ok(
                slalomResultService.getSecondRunStartList(competitionId)
        );
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<SlalomResultResponse>> getRanking(
            @PathVariable Long competitionId
    ) {
        return ResponseEntity.ok(
                slalomResultService.getRanking(competitionId)
        );
    }
}