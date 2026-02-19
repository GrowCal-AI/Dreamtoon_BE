package com.dreamtoon.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

/** 구독 취소 응답 */
@Getter
@Builder
public class CancelSubscriptionResponse {

    private String message;
    private boolean cancelAtPeriodEnd;
    private String subscriptionEndDate;
}
