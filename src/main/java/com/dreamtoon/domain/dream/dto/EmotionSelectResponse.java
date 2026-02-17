package com.dreamtoon.domain.dream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 감정 선택 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionSelectResponse {

    private String systemMessage;
}
