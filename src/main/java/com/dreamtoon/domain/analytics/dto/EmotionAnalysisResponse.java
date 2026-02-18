package com.dreamtoon.domain.analytics.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionAnalysisResponse {
    private String period;
    private Map<String, Integer> emotionDistribution;
    private List<DailyEmotionData> dailyData;

    @Getter
    @Builder
    public static class DailyEmotionData {
        private String date;
        private String primaryEmotion;
        private Integer sleepScore;
    }
}
