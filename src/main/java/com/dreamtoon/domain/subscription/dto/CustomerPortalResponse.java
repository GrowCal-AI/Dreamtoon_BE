package com.dreamtoon.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

/** Polar.sh Customer Portal URL 응답 */
@Getter
@Builder
public class CustomerPortalResponse {

    /** 이 URL로 리다이렉트하면 구독 관리 페이지로 이동 */
    private String portalUrl;
}
