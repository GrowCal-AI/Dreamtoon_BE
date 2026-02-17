package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 4컷 만화 생성 시작 응답 (비동기) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonGenerateResponse {

    private Long dreamId;
    private ProcessingStatus status;
    private String message;
}
