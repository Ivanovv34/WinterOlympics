package com.example.winter_olympics.result.service;

import com.example.winter_olympics.result.dto.BiathlonResultRequest;
import com.example.winter_olympics.result.dto.BiathlonResultResponse;

import java.util.List;

public interface BiathlonResultService {

    BiathlonResultResponse enterResult(Long competitionId, BiathlonResultRequest request);

    List<BiathlonResultResponse> getResults(Long competitionId);

    List<BiathlonResultResponse> calculateRanking(Long competitionId);

    List<BiathlonResultResponse> getRanking(Long competitionId);
}