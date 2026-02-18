package com.dreamtoon.domain.analytics.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthIndexResponse {
    private Integer stressLevel;
    private Integer anxietyLevel;
    private Integer emotionalResilience;
    private Integer relationshipStress;
    private Integer sleepQuality;
    private Double nightmareRatio;
    private List<String> insights;
    private Map<String, Integer> emotionDistribution;
}
