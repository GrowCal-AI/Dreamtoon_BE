package com.dreamtoon.domain.dream.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 꿈에서 감지되는 주요 감정 유형 */
@Getter
@RequiredArgsConstructor
public enum EmotionType {
    JOY("기쁨", "joy"),
    ANXIETY("불안", "anxiety"),
    ANGER("분노", "anger"),
    SADNESS("슬픔", "sadness"),
    DISCOMFORT("불편", "discomfort"),
    PEACE("평온", "peace");

    private final String description;
    private final String code;
}
