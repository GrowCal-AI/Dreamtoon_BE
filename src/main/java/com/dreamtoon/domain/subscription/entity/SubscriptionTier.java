package com.dreamtoon.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 구독 티어 (FREE → PLUS → PRO → ULTRA) */
@Getter
@RequiredArgsConstructor
public enum SubscriptionTier {
    /** 무료 회원 스탠다드 월 1회, 프리미엄 최초 1회(trial), 라이브러리 10개, 즐겨찾기 10개 */
    FREE(1, 0, 10, 10, 1, false),

    /** Plus ₩1,990/월 스탠다드 월 5회, 프리미엄 월 1회, 라이브러리 20개, 즐겨찾기 무제한, 감정분석 5회 */
    PLUS(5, 1, 20, -1, 5, false),

    /** Pro ₩9,900/월 스탠다드 월 20회, 프리미엄 월 5회, 라이브러리 무제한, 즐겨찾기 무제한, 감정분석 무제한 */
    PRO(20, 5, -1, -1, -1, true),

    /** Ultra ₩19,900/월 스탠다드 무제한, 프리미엄 월 20회, 라이브러리 무제한, 즐겨찾기 무제한, 감정분석 무제한 */
    ULTRA(-1, 20, -1, -1, -1, true);

    private final int maxStandardGenerations; // -1 = 무제한
    private final int maxPremiumGenerations; // 0 = trial 소진 후 불가, -1 = 무제한
    private final int maxLibraryItems; // -1 = 무제한
    private final int maxFavorites; // -1 = 무제한
    private final int maxAnalysis; // -1 = 무제한
    private final boolean dashboardAllowed;

    public boolean isUnlimitedStandard() {
        return maxStandardGenerations == -1;
    }

    public boolean isUnlimitedPremium() {
        return maxPremiumGenerations == -1;
    }

    public boolean isUnlimitedLibrary() {
        return maxLibraryItems == -1;
    }

    public boolean isUnlimitedFavorites() {
        return maxFavorites == -1;
    }

    public boolean isUnlimitedAnalysis() {
        return maxAnalysis == -1;
    }

    /** Plus 이상: 프리미엄 필터 월 쿼터 사용 가능 여부 */
    public boolean canUsePremiumFilter() {
        return this != FREE;
    }
}
