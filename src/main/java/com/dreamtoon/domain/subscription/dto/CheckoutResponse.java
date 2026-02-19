package com.dreamtoon.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

/** Polar.sh 결제 체크아웃 URL 응답 */
@Getter
@Builder
public class CheckoutResponse {

    /** Polar.sh 체크아웃 세션 ID */
    private String checkoutId;

    /** 사용자를 이 URL로 리다이렉트하면 결제 페이지로 이동 */
    private String checkoutUrl;

    private String tier;
}
