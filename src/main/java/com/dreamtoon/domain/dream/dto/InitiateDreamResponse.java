package com.dreamtoon.domain.dream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 꿈 기록 시작 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateDreamResponse {

    private Long dreamId;
    private String systemMessage;
}
