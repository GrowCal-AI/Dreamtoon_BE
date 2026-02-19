package com.dreamtoon.domain.subscription.dto;

import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 결제 체크아웃 URL 생성 요청 */
@Getter
@NoArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "구독 티어를 선택해주세요")
    private SubscriptionTier tier;
}
