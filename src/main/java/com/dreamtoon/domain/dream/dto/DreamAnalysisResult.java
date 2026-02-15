package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.scene.dto.DialogueDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * GPT-4o 꿈 분석 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DreamAnalysisResult {

    private List<SceneDto> scenes;
    private AnalysisDto analysis;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneDto {
        private Integer sceneNumber;
        private String description;
        private List<String> characters;
        private EmotionType emotion;
        private List<String> backgroundKeywords;
        private String narration;
        private List<DialogueDto> dialogue;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisDto {
        private Map<EmotionType, Double> emotions;
        private Integer tensionLevel;
        private Integer controlLevel;
        private Boolean isNightmare;
        private List<String> repeatingSymbols;
        private List<String> relationshipPatterns;
        private Boolean hasResolution;
        private String aiInsight;
    }
}
