package com.dreamtoon.domain.analysis.dto;

import com.dreamtoon.domain.analysis.entity.Analysis;
import com.dreamtoon.domain.dream.entity.EmotionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AnalysisResponse {

    private Long analysisId;
    private Integer healthScore;
    private Map<EmotionType, Double> emotions;  // String 대신 EmotionType 사용
    private Integer tensionLevel;
    private Integer controlLevel;
    private Boolean isNightmare;
    private List<String> repeatingSymbols;
    private List<String> relationshipPatterns;
    private Boolean hasResolution;
    private String aiInsight;

    public static AnalysisResponse from(Analysis analysis) {
        return AnalysisResponse.builder()
                .analysisId(analysis.getId())
                .healthScore(analysis.getHealthScore())
                .emotions(analysis.getEmotions())
                .tensionLevel(analysis.getTensionLevel())
                .controlLevel(analysis.getControlLevel())
                .isNightmare(analysis.getIsNightmare())
                .repeatingSymbols(analysis.getRepeatingSymbols())
                .relationshipPatterns(analysis.getRelationshipPatterns())
                .hasResolution(analysis.getHasResolution())
                .aiInsight(analysis.getAiInsight())
                .build();
    }
}
