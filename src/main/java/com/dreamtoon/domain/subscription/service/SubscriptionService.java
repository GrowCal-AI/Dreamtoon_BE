package com.dreamtoon.domain.subscription.service;

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

    /**
     * 사용자 구독 정보 조회 (없으면 자동 생성)
     *
     * @param userId 사용자 ID
     * @return 구독 정보
     */
    @Transactional
    public Subscription getOrCreateSubscription(Long userId) {
        return subscriptionRepository
                .findByUserId(userId)
                .orElseGet(() -> createDefaultSubscription(userId));
    }

    /**
     * 사용량 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용량 응답
     */
    public UsageResponse getUsage(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return UsageResponse.from(subscription);
    }

    /**
     * 기본 구독 생성 (무료 티어)
     *
     * @param userId 사용자 ID
     * @return 생성된 구독
     */
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

    /**
     * 생성 가능 여부 확인
     *
     * @param userId 사용자 ID
     * @return 생성 가능 여부
     */
    public boolean canGenerate(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canGenerate();
    }

    /**
     * 저장 가능 여부 확인
     *
     * @param userId 사용자 ID
     * @return 저장 가능 여부
     */
    public boolean canSave(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canAddToLibrary();
    }

    /** 라이브러리 추가 가능 여부 */
    public boolean canAddToLibrary(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canAddToLibrary();
    }

    /** 즐겨찾기 추가 가능 여부 */
    public boolean canFavorite(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canFavorite();
    }

    /** 프리미엄 기능 사용 가능 여부 */
    public boolean canUsePremiumStyles(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        return subscription.canUsePremiumFeatures();
    }

    /**
     * 생성 횟수 증가 (생성 제한 체크 포함)
     *
     * @param userId 사용자 ID
     * @throws BusinessException 생성 제한 초과 시
     */
    @Transactional
    public void incrementGenerationCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);

        if (!subscription.canGenerate()) {
            throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
        }

        subscription.incrementGenerationCount();
        subscriptionRepository.save(subscription);
        log.info(
                "Incremented generation count for user ID: {}, new count: {}",
                userId,
                subscription.getGenerationCount());
    }

    /**
     * 라이브러리 저장 개수 증가 (저장 제한 체크 포함)
     *
     * @param userId 사용자 ID
     * @throws BusinessException 저장 제한 초과 시
     */
    @Transactional
    public void incrementSavedCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);

        if (!subscription.canAddToLibrary()) {
            throw new BusinessException(ErrorCode.LIBRARY_LIMIT_EXCEEDED);
        }

        subscription.incrementLibraryCount();
        subscriptionRepository.save(subscription);
        log.info(
                "Incremented library count for user ID: {}, new count: {}",
                userId,
                subscription.getLibraryCount());
    }

    /**
     * 라이브러리 저장 개수 감소
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void decrementSavedCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.decrementLibraryCount();
        subscriptionRepository.save(subscription);
        log.info(
                "Decremented library count for user ID: {}, new count: {}",
                userId,
                subscription.getLibraryCount());
    }

    /**
     * 즐겨찾기 개수 증가 (제한 체크 포함)
     *
     * @param userId 사용자 ID
     * @throws BusinessException 즐겨찾기 제한 초과 시
     */
    @Transactional
    public void incrementFavoriteCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        if (!subscription.canFavorite()) {
            throw new BusinessException(ErrorCode.FAVORITE_LIMIT_EXCEEDED);
        }
        subscription.incrementFavoriteCount();
        subscriptionRepository.save(subscription);
    }

    /**
     * 즐겨찾기 개수 감소
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void decrementFavoriteCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.decrementFavoriteCount();
        subscriptionRepository.save(subscription);
    }

    /**
     * 월별 생성 횟수 초기화 (스케줄러에서 호출)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void resetMonthlyGenerationCount(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.resetMonthlyGenerationCount();
        subscriptionRepository.save(subscription);
        log.info("Reset monthly generation count for user ID: {}", userId);
    }

    /**
     * 프리미엄으로 업그레이드
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void upgradeToPremium(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.upgradeToPremium();
        subscriptionRepository.save(subscription);
        log.info("Upgraded user ID: {} to PREMIUM", userId);
    }

    /**
     * 무료로 다운그레이드
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void downgradeToFree(Long userId) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.downgradeToFree();
        subscriptionRepository.save(subscription);
        log.info("Downgraded user ID: {} to FREE", userId);
    }
}
