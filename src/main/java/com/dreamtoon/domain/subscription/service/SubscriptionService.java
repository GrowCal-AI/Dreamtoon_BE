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

    // ── 스탠다드 이미지 쿼터 ──

    @Transactional
    public boolean canGenerateStandard(Long userId) {
        return getOrCreateSubscription(userId).canGenerateStandard();
    }

    @Transactional
    public void incrementStandardGeneration(Long userId) {
        Subscription sub = getOrCreateSubscription(userId);
        if (!sub.canGenerateStandard()) {
            throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
        }
        sub.incrementStandardGenerationCount();
        subscriptionRepository.save(sub);
    }

    // ── 프리미엄 이미지 쿼터 ──

    @Transactional
    public boolean canGeneratePremium(Long userId) {
        return getOrCreateSubscription(userId).canGeneratePremium();
    }

    @Transactional
    public void incrementPremiumGeneration(Long userId) {
        Subscription sub = getOrCreateSubscription(userId);
        if (!sub.canGeneratePremium()) {
            throw new BusinessException(ErrorCode.PREMIUM_STYLE_NOT_ALLOWED);
        }
        sub.incrementPremiumGenerationCount();
        subscriptionRepository.save(sub);
    }

    // ── 라이브러리 / 즐겨찾기 ──

    @Transactional
    public boolean canAddToLibrary(Long userId) {
        Subscription sub = getOrCreateSubscription(userId);
        long libraryCount = dreamRepository.countLibraryByUserId(userId);
        return sub.canAddToLibrary(libraryCount);
    }

    @Transactional
    public boolean canFavorite(Long userId) {
        Subscription sub = getOrCreateSubscription(userId);
        long favoriteCount = dreamRepository.countFavoritesByUserId(userId);
        return sub.canFavorite(favoriteCount);
    }

    // ── 월별 쿼터 리셋 (스케줄러 호출) ──

    @Transactional
    public void resetAllDueQuotas() {
        java.time.LocalDate today = java.time.LocalDate.now();

        // 1) 쿼터 리셋 대상 처리
        java.util.List<Subscription> due = subscriptionRepository.findAllDueForReset(today);
        for (Subscription sub : due) {
            sub.resetMonthlyQuota();
        }
        subscriptionRepository.saveAll(due);
        log.info("Monthly quota reset completed for {} subscriptions", due.size());

        // 2) 구독 기간 만료 → FREE 다운그레이드
        java.util.List<Subscription> expired = subscriptionRepository.findAllExpired(today);
        for (Subscription sub : expired) {
            log.info("Subscription expired, downgrading to FREE: userId={}", sub.getUser().getId());
            sub.revokeSubscription();
        }
        subscriptionRepository.saveAll(expired);
        log.info("Expired subscription downgrade completed for {} subscriptions", expired.size());
    }

    // ── 관리자/테스트용 티어 강제 변경 ──

    @Transactional
    public void forceSetTier(Long userId, SubscriptionTier tier) {
        Subscription sub = getOrCreateSubscription(userId);
        sub.forceSetTier(tier);
        subscriptionRepository.save(sub);
        log.info("[ADMIN] Force set tier for user ID: {} → {}", userId, tier);
    }

    // ── Polar.sh Webhook 동기화 ──

    @Transactional
    public void syncFromPolar(
            String polarSubscriptionId,
            String polarCustomerId,
            SubscriptionTier tier,
            java.time.LocalDate endDate,
            boolean cancelAtPeriodEnd) {
        subscriptionRepository
                .findByPolarSubscriptionId(polarSubscriptionId)
                .ifPresent(
                        sub -> {
                            sub.activateSubscription(
                                    polarSubscriptionId, polarCustomerId, tier, endDate);
                            sub.markCancelAtPeriodEnd(cancelAtPeriodEnd);
                            subscriptionRepository.save(sub);
                            log.info("Synced subscription {} → tier={}", polarSubscriptionId, tier);
                        });
    }

    @Transactional
    public void revokeFromPolar(String polarSubscriptionId) {
        subscriptionRepository
                .findByPolarSubscriptionId(polarSubscriptionId)
                .ifPresent(
                        sub -> {
                            sub.revokeSubscription();
                            subscriptionRepository.save(sub);
                            log.info("Revoked subscription {} → FREE", polarSubscriptionId);
                        });
    }

    /** Polar 신규 구독 생성 시 userId로 구독을 찾아서 Polar 정보와 연결 */
    @Transactional
    public void activateNewSubscription(
            Long userId,
            String polarSubscriptionId,
            String polarCustomerId,
            SubscriptionTier tier,
            java.time.LocalDate endDate) {
        Subscription sub = getOrCreateSubscription(userId);
        sub.activateSubscription(polarSubscriptionId, polarCustomerId, tier, endDate);
        subscriptionRepository.save(sub);
        log.info("Activated new subscription for user ID: {} → tier={}", userId, tier);
    }

    // ── Polar.sh 결제 연동 ──

    /**
     * 결제 체크아웃 URL 생성.
     *
     * @param userId 현재 로그인 사용자 ID
     * @param tier 구독할 티어 (PLUS / PRO / ULTRA)
     * @param polarApiClient Polar API 클라이언트 (순환 의존성 방지를 위해 파라미터로 주입)
     * @param polarProperties Polar 설정
     * @return 체크아웃 URL 응답 DTO
     */
    public com.dreamtoon.domain.subscription.dto.CheckoutResponse createCheckoutUrl(
            Long userId,
            SubscriptionTier tier,
            com.dreamtoon.infrastructure.payment.PolarApiClient polarApiClient,
            com.dreamtoon.global.config.PolarProperties polarProperties) {
        if (tier == SubscriptionTier.FREE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new com.dreamtoon.global.error.EntityNotFoundException(
                                                ErrorCode.USER_NOT_FOUND));

        String productId =
                switch (tier) {
                    case PLUS -> polarProperties.getProducts().getPlus();
                    case PRO -> polarProperties.getProducts().getPro();
                    case ULTRA -> polarProperties.getProducts().getUltra();
                    default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
                };

        try {
            com.fasterxml.jackson.databind.JsonNode result =
                    polarApiClient.createCheckout(productId, user.getEmail(), userId);
            return com.dreamtoon.domain.subscription.dto.CheckoutResponse.builder()
                    .checkoutId(result.path("id").asText())
                    .checkoutUrl(result.path("url").asText())
                    .tier(tier.name())
                    .build();
        } catch (Exception e) {
            log.error("Polar checkout 생성 실패: userId={}, tier={}", userId, tier, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * Customer Portal URL 생성 (구독 관리/해지 페이지).
     *
     * @param userId 현재 로그인 사용자 ID
     */
    public com.dreamtoon.domain.subscription.dto.CustomerPortalResponse createCustomerPortalUrl(
            Long userId, com.dreamtoon.infrastructure.payment.PolarApiClient polarApiClient) {
        Subscription sub = getOrCreateSubscription(userId);
        String polarCustomerId = sub.getPolarCustomerId();

        if (polarCustomerId == null || polarCustomerId.isBlank()) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        try {
            com.fasterxml.jackson.databind.JsonNode result =
                    polarApiClient.createCustomerPortalSession(polarCustomerId);
            return com.dreamtoon.domain.subscription.dto.CustomerPortalResponse.builder()
                    .portalUrl(result.path("url").asText())
                    .build();
        } catch (Exception e) {
            log.error("Polar portal 생성 실패: userId={}", userId, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
