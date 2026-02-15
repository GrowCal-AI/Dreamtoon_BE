package com.dreamtoon.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 구독 티어 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionTier {
    /** 무료 회원 - 월 3회 생성, 최대 3개 저장, 기본 스타일만 */
    FREE(3, 3, false),

    /** 프리미엄 회원 - 무제한 생성, 무제한 저장, 모든 스타일 사용 가능 */
    PREMIUM(-1, -1, true);

    private final int maxGenerations; // -1은 무제한
    private final int maxSavedDreams; // -1은 무제한
    private final boolean premiumStylesAllowed;

    public boolean isUnlimitedGenerations() {
        return maxGenerations == -1;
    }

    public boolean isUnlimitedSaves() {
        return maxSavedDreams == -1;
    }
}
