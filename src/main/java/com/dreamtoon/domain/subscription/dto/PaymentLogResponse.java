package com.dreamtoon.domain.subscription.dto;

import com.dreamtoon.domain.subscription.entity.PaymentEventType;
import com.dreamtoon.domain.subscription.entity.PaymentLog;
import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/** 결제 로그 응답 DTO */
@Getter
@Builder
public class PaymentLogResponse {

    private Long id;
    private PaymentEventType eventType;
    private SubscriptionTier tier;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;

    public static PaymentLogResponse from(PaymentLog paymentLog) {
        return PaymentLogResponse.builder()
                .id(paymentLog.getId())
                .eventType(paymentLog.getEventType())
                .tier(paymentLog.getTier())
                .description(paymentLog.getDescription())
                .metadata(paymentLog.getMetadata())
                .createdAt(paymentLog.getCreatedAt())
                .build();
    }
}
