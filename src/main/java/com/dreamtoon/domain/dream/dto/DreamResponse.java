package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.analysis.dto.AnalysisResponse;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.StylePreset;
import com.dreamtoon.domain.scene.dto.SceneResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DreamResponse {
    
    private Long dreamId;
    private String rawContent;
    private StylePreset stylePreset;
    private List<SceneResponse> scenes;
    private AnalysisResponse analysis;
    private LocalDateTime createdAt;
    
    public static DreamResponse from(Dream dream) {
        return DreamResponse.builder()
                .dreamId(dream.getId())
                .rawContent(dream.getRawContent())
                .stylePreset(dream.getStylePreset())
                .createdAt(dream.getCreatedAt())
                .build();
    }
    
    public static DreamResponse fromWithDetails(Dream dream) {
        return DreamResponse.builder()
                .dreamId(dream.getId())
                .rawContent(dream.getRawContent())
                .stylePreset(dream.getStylePreset())
                .scenes(dream.getScenes().stream()
                        .map(SceneResponse::from)
                        .collect(Collectors.toList()))
                .analysis(dream.getAnalysis() != null ? AnalysisResponse.from(dream.getAnalysis()) : null)
                .createdAt(dream.getCreatedAt())
                .build();
    }
}
