package com.dreamtoon.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 결제 이벤트 타입 */
@Getter
@RequiredArgsConstructor
public enum PaymentEventType {
    CHECKOUT_CREATED("결제 체크아웃 생성"),
    SUBSCRIPTION_CREATED("구독 생성"),
    SUBSCRIPTION_UPDATED("구독 업데이트"),
    SUBSCRIPTION_CANCELED("구독 취소 (기간 만료 시 해지)"),
    SUBSCRIPTION_REVOKED("구독 즉시 해지"),
    SUBSCRIPTION_EXPIRED("구독 만료 다운그레이드"),
    CANCELLATION_REQUESTED("사용자 직접 취소 요청"),
    TIER_CHANGED("관리자 티어 변경"),
    QUOTA_RESET("월별 쿼터 리셋"),
    PAYMENT_FAILED("결제/처리 실패");

    private final String description;
}
