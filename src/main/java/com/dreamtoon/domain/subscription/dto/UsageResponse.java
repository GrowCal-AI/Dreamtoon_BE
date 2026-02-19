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
    private Integer standardGenerationCount;
    private Integer maxStandardGenerations;
    private Integer premiumGenerationCount;
    private Integer maxPremiumGenerations;
    private Boolean premiumTrialUsed;
    private Long libraryCount;
    private Integer maxLibrary;
    private Long favoriteCount;
    private Integer maxFavorites;
    private LocalDate quotaResetDate;
    private Boolean canGenerateStandard;
    private Boolean canGeneratePremium;
    private Boolean canAddToLibrary;
    private Boolean canFavorite;

    public static UsageResponse from(
            Subscription subscription, long libraryCount, long favoriteCount) {
        SubscriptionTier tier = subscription.getTier();
        return UsageResponse.builder()
                .tier(tier)
                .isActive(subscription.getIsActive())
                .standardGenerationCount(subscription.getStandardGenerationCount())
                .maxStandardGenerations(tier.getMaxStandardGenerations())
                .premiumGenerationCount(subscription.getPremiumGenerationCount())
                .maxPremiumGenerations(tier.getMaxPremiumGenerations())
                .premiumTrialUsed(subscription.getPremiumTrialUsed())
                .libraryCount(libraryCount)
                .maxLibrary(tier.getMaxLibraryItems())
                .favoriteCount(favoriteCount)
                .maxFavorites(tier.getMaxFavorites())
                .quotaResetDate(subscription.getQuotaResetDate())
                .canGenerateStandard(subscription.canGenerateStandard())
                .canGeneratePremium(subscription.canGeneratePremium())
                .canAddToLibrary(subscription.canAddToLibrary(libraryCount))
                .canFavorite(subscription.canFavorite(favoriteCount))
                .build();
    }
}
