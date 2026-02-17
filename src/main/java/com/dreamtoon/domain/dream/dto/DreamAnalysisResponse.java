package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 꿈 분석 결과 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DreamAnalysisResponse {

    private Long dreamId;
    private ProcessingStatus status;
    private String title;
    private String aiAnalysis;
    private Map<String, Integer> emotionScores;
    private String aiInsight;
}
