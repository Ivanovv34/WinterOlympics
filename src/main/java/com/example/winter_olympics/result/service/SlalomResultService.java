package com.example.winter_olympics.result.service;

import com.example.winter_olympics.result.dto.SlalomFirstRunRequest;
import com.example.winter_olympics.result.dto.SlalomQualificationRequest;
import com.example.winter_olympics.result.dto.SlalomResultResponse;
import com.example.winter_olympics.result.dto.SlalomSecondRunRequest;

import java.util.List;

public interface SlalomResultService {

    SlalomResultResponse enterFirstRunResult(Long competitionId, SlalomFirstRunRequest request);

    List<SlalomResultResponse> qualifyForSecondRun(Long competitionId, SlalomQualificationRequest request);

    SlalomResultResponse enterSecondRunResult(Long competitionId, SlalomSecondRunRequest request);

    List<SlalomResultResponse> getSlalomResults(Long competitionId);

    List<SlalomResultResponse> getSecondRunStartList(Long competitionId);

    List<SlalomResultResponse> calculateRanking(Long competitionId);

    List<SlalomResultResponse> getRanking(Long competitionId);
}