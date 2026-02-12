package com.dreamtoon.domain.analysis.dto;

import com.dreamtoon.domain.analysis.entity.Analysis;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AnalysisResponse {
    
    private Long analysisId;
    private Integer healthScore;
    private Map<String, Double> emotions;
    private String aiInsight;
    
    public static AnalysisResponse from(Analysis analysis) {
        return AnalysisResponse.builder()
                .analysisId(analysis.getId())
                .healthScore(analysis.getHealthScore())
                .emotions(analysis.getEmotions())
                .aiInsight(analysis.getAiInsight())
                .build();
    }
}
