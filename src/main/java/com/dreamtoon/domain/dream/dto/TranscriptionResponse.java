package com.dreamtoon.domain.dream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 음성 전사 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {

    /** 전사된 텍스트 */
    private String text;

    /** 전사 언어 (예: "ko", "en") */
    private String language;

    /** 전사 소요 시간 (밀리초) */
    private Long processingTimeMs;
}
