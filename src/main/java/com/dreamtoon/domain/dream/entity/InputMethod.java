package com.dreamtoon.domain.dream.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 꿈 입력 방식 */
@Getter
@RequiredArgsConstructor
public enum InputMethod {
        TEXT("텍스트 입력"),
        VOICE("음성 입력");

        private final String description;
}
