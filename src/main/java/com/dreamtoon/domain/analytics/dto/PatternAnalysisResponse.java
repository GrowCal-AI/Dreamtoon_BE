package com.dreamtoon.domain.analytics.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatternAnalysisResponse {
    private List<String> repeatingSymbols;
    private List<String> relationshipPatterns;
    private Integer totalDreamCount;
    private Double nightmareRatio;
}
