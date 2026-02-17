package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DreamResponse {

    private Long dreamId;
    private Long userId;
    private String title;
    private String dreamContent;
    private String primaryEmotion;
    private String detailedDescription;
    private String realLifeContext;
    private String aiAnalysis;
    private Map<String, Integer> emotionScores;
    private String aiInsight;
    private Genre selectedGenre;
    private List<String> webtoonImages;
    private Boolean isFavorite;
    private Boolean isInLibrary;
    private ProcessingStatus processingStatus;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DreamResponse from(Dream dream) {
        return DreamResponse.builder()
                .dreamId(dream.getId())
                .userId(dream.getUser() != null ? dream.getUser().getId() : null)
                .title(dream.getTitle())
                .dreamContent(dream.getDreamContent())
                .primaryEmotion(
                        dream.getPrimaryEmotion() != null ? dream.getPrimaryEmotion().name() : null)
                .detailedDescription(dream.getDetailedDescription())
                .realLifeContext(dream.getRealLifeContext())
                .aiAnalysis(dream.getAiAnalysis())
                .emotionScores(dream.getEmotionScores())
                .aiInsight(dream.getAiInsight())
                .selectedGenre(dream.getSelectedGenre())
                .webtoonImages(dream.getWebtoonImages())
                .isFavorite(dream.getIsFavorite())
                .isInLibrary(dream.getIsInLibrary())
                .processingStatus(dream.getProcessingStatus())
                .errorMessage(dream.getErrorMessage())
                .createdAt(dream.getCreatedAt())
                .updatedAt(dream.getUpdatedAt())
                .build();
    }
}
