package com.dreamtoon.domain.analysis.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DreamAnalysisResponse {
    private boolean hasEnoughData;
    private String message;
    private int stressIndex;
    private String stressLevel; // LOW, MEDIUM, HIGH
    private int sleepQualityScore;
    private String sleepQualityMessage;
    private EmotionBalance emotionBalance;
    private List<WeeklyDreamStat> weeklyDreamFlow;
    private String aiCoachMessage;

    @Getter
    @Builder
    public static class EmotionBalance {
        private int joy;
        private int anxiety;
        private int anger;
        private int sadness;
        private int discomfort;
        private int peace;
    }

    @Getter
    @Builder
    public static class WeeklyDreamStat {
        private LocalDate date;
        private boolean hasDream;
        private String primaryEmotion; // EmotionType code or description
    }
}
