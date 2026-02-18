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
    private Integer maxGenerations;
    private Long libraryCount;
    private Integer maxLibrary;
    private Long favoriteCount;
    private Integer maxFavorites;
    private LocalDate quotaResetDate;
    private Boolean canGenerate;
    private Boolean canAddToLibrary;
    private Boolean canFavorite;
    private Boolean canUsePremiumFeatures;

    public static UsageResponse from(
            Subscription subscription, long libraryCount, long favoriteCount) {
        return UsageResponse.builder()
                .tier(subscription.getTier())
                .isActive(subscription.getIsActive())
                .generationCount(subscription.getGenerationCount())
                .maxGenerations(subscription.getTier().getMaxGenerations())
                .libraryCount(libraryCount)
                .maxLibrary(subscription.getTier().getMaxLibraryItems())
                .favoriteCount(favoriteCount)
                .maxFavorites(subscription.getTier().getMaxFavorites())
                .quotaResetDate(subscription.getQuotaResetDate())
                .canGenerate(subscription.canGenerate())
                .canAddToLibrary(subscription.canAddToLibrary(libraryCount))
                .canFavorite(subscription.canFavorite(favoriteCount))
                .canUsePremiumFeatures(subscription.canUsePremiumFeatures())
                .build();
    }
}
