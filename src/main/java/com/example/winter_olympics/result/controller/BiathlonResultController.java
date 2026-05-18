package com.example.winter_olympics.result.controller;

import com.example.winter_olympics.result.dto.BiathlonResultRequest;
import com.example.winter_olympics.result.dto.BiathlonResultResponse;
import com.example.winter_olympics.result.service.BiathlonResultService;
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
@RequestMapping("/api/competitions/{competitionId}/biathlon")
public class BiathlonResultController {

    private final BiathlonResultService biathlonResultService;

    @PostMapping("/results")
    public ResponseEntity<BiathlonResultResponse> enterResult(
            @PathVariable Long competitionId,
            @Valid @RequestBody BiathlonResultRequest request
    ) {
        return ResponseEntity.ok(
                biathlonResultService.enterResult(competitionId, request)
        );
    }

    @GetMapping("/results")
    public ResponseEntity<List<BiathlonResultResponse>> getResults(
            @PathVariable Long competitionId
    ) {
        return ResponseEntity.ok(
                biathlonResultService.getResults(competitionId)
        );
    }
}