package com.dreamtoon.domain.conversation.dto;

import com.dreamtoon.domain.conversation.entity.StressAssessment;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StressAssessmentResponse {

    private Integer totalStressIndex;
    private List<String> topStressors;
    private String sleepQualityLevel;
    private String aiRecommendation;

    public static StressAssessmentResponse from(
            StressAssessment assessment, String aiRecommendation) {
        return StressAssessmentResponse.builder()
                .totalStressIndex(assessment.calculateTotalStressIndex())
                .topStressors(assessment.getTopStressors())
                .sleepQualityLevel(assessment.getSleepQualityLevel())
                .aiRecommendation(aiRecommendation)
                .build();
    }
}
