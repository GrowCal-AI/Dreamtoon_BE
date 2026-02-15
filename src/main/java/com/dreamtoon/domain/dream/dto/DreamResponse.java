package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.analysis.dto.AnalysisResponse;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.InputMethod;
import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import com.dreamtoon.domain.dream.entity.StylePreset;
import com.dreamtoon.domain.scene.dto.SceneResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DreamResponse {

        private Long dreamId;
        private Long userId;
        private String title;
        private String content; // rawContent를 content로 변경 (프론트엔드와 일치)
        private StylePreset stylePreset;
        private InputMethod inputMethod;
        private List<String> tags;
        private Boolean isFavorite;
        private ProcessingStatus processingStatus;
        private String errorMessage;
        private String webtoonUrl;
        private String videoUrl;
        private LocalDateTime recordedAt;
        private LocalDateTime createdAt;
        private List<SceneResponse> scenes;
        private AnalysisResponse analysis;

        public static DreamResponse from(Dream dream) {
                return DreamResponse.builder()
                                .dreamId(dream.getId())
                                .userId(dream.getUser() != null ? dream.getUser().getId() : null)
                                .title(dream.getTitle())
                                .content(dream.getRawContent())
                                .stylePreset(dream.getStylePreset())
                                .inputMethod(dream.getInputMethod())
                                .tags(dream.getTags())
                                .isFavorite(dream.getIsFavorite())
                                .processingStatus(dream.getProcessingStatus())
                                .errorMessage(dream.getErrorMessage())
                                .webtoonUrl(dream.getWebtoonUrl())
                                .videoUrl(dream.getVideoUrl())
                                .recordedAt(dream.getRecordedAt())
                                .createdAt(dream.getCreatedAt())
                                .build();
        }

        public static DreamResponse fromWithDetails(Dream dream) {
                return DreamResponse.builder()
                                .dreamId(dream.getId())
                                .userId(dream.getUser() != null ? dream.getUser().getId() : null)
                                .title(dream.getTitle())
                                .content(dream.getRawContent())
                                .stylePreset(dream.getStylePreset())
                                .inputMethod(dream.getInputMethod())
                                .tags(dream.getTags())
                                .isFavorite(dream.getIsFavorite())
                                .processingStatus(dream.getProcessingStatus())
                                .errorMessage(dream.getErrorMessage())
                                .webtoonUrl(dream.getWebtoonUrl())
                                .videoUrl(dream.getVideoUrl())
                                .recordedAt(dream.getRecordedAt())
                                .createdAt(dream.getCreatedAt())
                                .scenes(
                                                dream.getScenes().stream()
                                                                .map(SceneResponse::from)
                                                                .collect(Collectors.toList()))
                                .analysis(
                                                dream.getAnalysis() != null
                                                                ? AnalysisResponse.from(dream.getAnalysis())
                                                                : null)
                                .build();
        }
}
