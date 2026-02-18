package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.Dream;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/** FE DreamEntry와 1:1 매핑되는 응답 DTO */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DreamResponse {

    private String id;
    private String userId;
    private String title;
    private String content;
    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;
    private String inputMethod;
    private String style;
    private String format;
    private List<SceneData> scenes;
    private AnalysisData analysis;
    private String webtoonUrl;
    private List<String> tags;
    private Boolean isFavorite;
    private Boolean isInLibrary;
    private String processingStatus;
    private String errorMessage;

    @Getter
    @Builder
    public static class SceneData {
        private String id;
        private Integer sceneNumber;
        private String description;
        private List<String> characters;
        private String emotion;
        private List<String> backgroundKeywords;
        private String imageUrl;
        private String narration;
        private List<DialogueDto> dialogue;
    }

    @Getter
    @Builder
    public static class DialogueDto {
        private String character;
        private String text;
    }

    @Getter
    @Builder
    public static class AnalysisData {
        private Map<String, Integer> emotions;
        private Integer tensionLevel;
        private Integer controlLevel;
        private Boolean isNightmare;
        private List<String> repeatingSymbols;
        private List<String> relationshipPatterns;
        private Boolean hasResolution;
        private String aiInsight;
    }

    public static DreamResponse from(Dream dream) {
        AnalysisData analysisData = null;
        if (dream.getEmotionScores() != null && !dream.getEmotionScores().isEmpty()) {
            analysisData =
                    AnalysisData.builder()
                            .emotions(dream.getEmotionScores())
                            .aiInsight(dream.getAiInsight())
                            .build();
        }

        List<SceneData> sceneList = buildScenesFromImages(dream);

        String firstImage =
                dream.getWebtoonImages() != null && !dream.getWebtoonImages().isEmpty()
                        ? dream.getWebtoonImages().get(0)
                        : null;

        return DreamResponse.builder()
                .id(String.valueOf(dream.getId()))
                .userId(dream.getUser() != null ? String.valueOf(dream.getUser().getId()) : null)
                .title(dream.getTitle())
                .content(dream.getDreamContent())
                .recordedAt(dream.getCreatedAt())
                .createdAt(dream.getCreatedAt())
                .inputMethod("text")
                .style(dream.getSelectedGenre() != null ? dream.getSelectedGenre().getCode() : null)
                .format("webtoon")
                .scenes(sceneList)
                .analysis(analysisData)
                .webtoonUrl(firstImage)
                .tags(dream.getTags())
                .isFavorite(dream.getIsFavorite())
                .isInLibrary(dream.getIsInLibrary())
                .processingStatus(
                        dream.getProcessingStatus() != null
                                ? dream.getProcessingStatus().name()
                                : null)
                .errorMessage(dream.getErrorMessage())
                .build();
    }

    private static List<SceneData> buildScenesFromImages(Dream dream) {
        if (dream.getWebtoonImages() == null || dream.getWebtoonImages().isEmpty()) {
            return Collections.emptyList();
        }
        List<SceneData> result = new ArrayList<>();
        for (int i = 0; i < dream.getWebtoonImages().size(); i++) {
            result.add(
                    SceneData.builder()
                            .id(String.valueOf(i + 1))
                            .sceneNumber(i + 1)
                            .imageUrl(dream.getWebtoonImages().get(i))
                            .build());
        }
        return result;
    }
}
