package com.dreamtoon.domain.subscription.service;

import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.subscription.dto.UsageResponse;
import com.dreamtoon.domain.subscription.entity.Subscription;
import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import com.dreamtoon.domain.subscription.repository.SubscriptionRepository;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.error.BusinessException;
import com.dreamtoon.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 구독 관리 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final DreamRepository dreamRepository;

    @Transactional
    public Subscription getOrCreateSubscription(Long userId) {
        return subscriptionRepository
                .findByUserId(userId)
                .orElseGet(() -> createDefaultSubscription(userId));
    }

    @Transactional
    public UsageResponse getUsage(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        long libraryCount = dreamRepository.countLibraryByUserId(userId);
        long favoriteCount = dreamRepository.countFavoritesByUserId(userId);
        return UsageResponse.from(subscription, libraryCount, favoriteCount);
    }

    @Transactional
    public Subscription createDefaultSubscription(Long userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription =
                Subscription.builder().user(user).tier(SubscriptionTier.FREE).build();

        subscriptionRepository.save(subscription);
        log.info("Created default FREE subscription for user ID: {}", userId);
        return subscription;
    }

    @Transactional
    public boolean canGenerate(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canGenerate();
    }

    @Transactional
    public boolean canAddToLibrary(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        long libraryCount = dreamRepository.countLibraryByUserId(userId);
        return subscription.canAddToLibrary(libraryCount);
    }

    @Transactional
    public boolean canFavorite(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        long favoriteCount = dreamRepository.countFavoritesByUserId(userId);
        return subscription.canFavorite(favoriteCount);
    }

    public boolean canUsePremiumStyles(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canUsePremiumFeatures();
    }

    @Transactional
    public void incrementGenerationCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        if (!subscription.canGenerate()) {
            throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
        }
        subscription.incrementGenerationCount();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void resetMonthlyGenerationCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.resetMonthlyGenerationCount();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void upgradeToPremium(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.upgradeToPremium();
        subscriptionRepository.save(subscription);
        log.info("Upgraded user ID: {} to PREMIUM", userId);
    }

    @Transactional
    public void downgradeToFree(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.downgradeToFree();
        subscriptionRepository.save(subscription);
        log.info("Downgraded user ID: {} to FREE", userId);
    }
}
