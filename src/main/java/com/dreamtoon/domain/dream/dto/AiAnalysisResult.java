package com.dreamtoon.domain.dream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** GPT 꿈 분석 API 응답 파싱용 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResult {

    private String title;
    private String analysis;
    private Map<String, Integer> emotionScores;
    private String insight;

    @JsonProperty("analysis")
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    @JsonProperty("emotionScores")
    public void setEmotionScores(Map<String, Integer> emotionScores) {
        this.emotionScores = emotionScores;
    }

    @JsonProperty("insight")
    public void setInsight(String insight) {
        this.insight = insight;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }
}
