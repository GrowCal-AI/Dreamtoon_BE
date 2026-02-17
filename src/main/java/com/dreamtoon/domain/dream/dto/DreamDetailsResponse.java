package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상세 설명 저장 후 응답 (비동기 분석 시작) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DreamDetailsResponse {

    private Long dreamId;
    private ProcessingStatus status;
    private String message;
}
