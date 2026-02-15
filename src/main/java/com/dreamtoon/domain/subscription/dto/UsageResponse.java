package com.dreamtoon.domain.subscription.dto;

import com.dreamtoon.domain.subscription.entity.Subscription;
import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsageResponse {

    private SubscriptionTier tier;
    private Boolean isActive;
    private Integer generationCount;
    private Integer maxGenerations; // -1은 무제한
    private Integer savedDreamsCount;
    private Integer maxSavedDreams; // -1은 무제한
    private Boolean canGenerate;
    private Boolean canSave;
    private Boolean canUsePremiumStyles;

    public static UsageResponse from(Subscription subscription) {
        return UsageResponse.builder()
                .tier(subscription.getTier())
                .isActive(subscription.getIsActive())
                .generationCount(subscription.getGenerationCount())
                .maxGenerations(subscription.getTier().getMaxGenerations())
                .savedDreamsCount(subscription.getSavedDreamsCount())
                .maxSavedDreams(subscription.getTier().getMaxSavedDreams())
                .canGenerate(subscription.canGenerate())
                .canSave(subscription.canSave())
                .canUsePremiumStyles(subscription.canUsePremiumStyles())
                .build();
    }
}
