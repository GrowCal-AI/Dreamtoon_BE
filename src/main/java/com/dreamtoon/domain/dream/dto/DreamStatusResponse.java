package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** Dream 처리 상태 조회 응답 DTO */
@Getter
@Builder
public class DreamStatusResponse {

        private Long dreamId;
        private ProcessingStatus processingStatus;
        private String errorMessage;
        private Integer sceneCount;
        private Boolean hasAnalysis;
        private LocalDateTime createdAt;

        public static DreamStatusResponse from(Dream dream) {
                return DreamStatusResponse.builder()
                                .dreamId(dream.getId())
                                .processingStatus(dream.getProcessingStatus())
                                .errorMessage(dream.getErrorMessage())
                                .sceneCount(dream.getScenes() != null ? dream.getScenes().size() : 0)
                                .hasAnalysis(dream.getAnalysis() != null)
                                .createdAt(dream.getCreatedAt())
                                .build();
        }
}
