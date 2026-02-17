package com.dreamtoon.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 구독 티어 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionTier {
    /** 무료 회원 - 월 10회 생성, 최대 50개 라이브러리, 최대 10개 즐겨찾기 */
    FREE(10, 50, 10, false),

    /** 프리미엄 회원 - 무제한 생성, 무제한 저장, 모든 기능 사용 가능 */
    PREMIUM(-1, -1, -1, true);

    private final int maxGenerations; // -1은 무제한
    private final int maxLibraryItems; // -1은 무제한
    private final int maxFavorites; // -1은 무제한
    private final boolean premiumFeaturesAllowed;

    public boolean isUnlimitedGenerations() {
        return maxGenerations == -1;
    }

    public boolean isUnlimitedLibrary() {
        return maxLibraryItems == -1;
    }

    public boolean isUnlimitedFavorites() {
        return maxFavorites == -1;
    }
}
