package com.dreamtoon.domain.subscription.dto;

import com.dreamtoon.domain.subscription.entity.Subscription;
import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsageResponse {

    private SubscriptionTier tier;
    private Boolean isActive;
    private Integer generationCount;
    private Integer maxGenerations; // -1은 무제한
    private Integer libraryCount;
    private Integer maxLibrary; // -1은 무제한
    private Integer favoriteCount;
    private Integer maxFavorites; // -1은 무제한
    private LocalDate quotaResetDate;
    private Boolean canGenerate;
    private Boolean canAddToLibrary;
    private Boolean canFavorite;
    private Boolean canUsePremiumFeatures;

    public static UsageResponse from(Subscription subscription) {
        return UsageResponse.builder()
                .tier(subscription.getTier())
                .isActive(subscription.getIsActive())
                .generationCount(subscription.getGenerationCount())
                .maxGenerations(subscription.getTier().getMaxGenerations())
                .libraryCount(subscription.getLibraryCount())
                .maxLibrary(subscription.getTier().getMaxLibraryItems())
                .favoriteCount(subscription.getFavoriteCount())
                .maxFavorites(subscription.getTier().getMaxFavorites())
                .quotaResetDate(subscription.getQuotaResetDate())
                .canGenerate(subscription.canGenerate())
                .canAddToLibrary(subscription.canAddToLibrary())
                .canFavorite(subscription.canFavorite())
                .canUsePremiumFeatures(subscription.canUsePremiumFeatures())
                .build();
    }
}
