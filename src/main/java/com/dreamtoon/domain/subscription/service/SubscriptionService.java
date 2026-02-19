package com.dreamtoon.domain.subscription.service;

import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.subscription.dto.CancelSubscriptionResponse;
import com.dreamtoon.domain.subscription.dto.UsageResponse;
import com.dreamtoon.domain.subscription.entity.PaymentEventType;
import com.dreamtoon.domain.subscription.entity.Subscription;
import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import com.dreamtoon.domain.subscription.repository.SubscriptionRepository;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.error.BusinessException;
import com.dreamtoon.global.error.ErrorCode;
import com.dreamtoon.infrastructure.payment.PolarApiClient;
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
    private final PaymentLogService paymentLogService;

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
            paymentLogService.log(
                    sub.getUser().getId(),
                    sub.getId(),
                    PaymentEventType.QUOTA_RESET,
                    sub.getTier(),
                    "월별 쿼터 리셋");
        }
        subscriptionRepository.saveAll(due);
        log.info("Monthly quota reset completed for {} subscriptions", due.size());

        // 2) 구독 기간 만료 → FREE 다운그레이드
        java.util.List<Subscription> expired = subscriptionRepository.findAllExpired(today);
        for (Subscription sub : expired) {
            Long userId = sub.getUser().getId();
            SubscriptionTier oldTier = sub.getTier();
            log.info("Subscription expired, downgrading to FREE: userId={}", userId);
            sub.revokeSubscription();
            paymentLogService.log(
                    userId,
                    sub.getId(),
                    PaymentEventType.SUBSCRIPTION_EXPIRED,
                    oldTier,
                    "구독 만료 다운그레이드: " + oldTier + " → FREE");
        }
        subscriptionRepository.saveAll(expired);
        log.info("Expired subscription downgrade completed for {} subscriptions", expired.size());
    }

    // ── 관리자/테스트용 티어 강제 변경 ──

    @Transactional
    public void forceSetTier(Long userId, SubscriptionTier tier) {
        Subscription sub = getOrCreateSubscription(userId);
        SubscriptionTier oldTier = sub.getTier();
        sub.forceSetTier(tier);
        subscriptionRepository.save(sub);
        log.info("[ADMIN] Force set tier for user ID: {} → {}", userId, tier);
        paymentLogService.log(
                userId,
                sub.getId(),
                PaymentEventType.TIER_CHANGED,
                tier,
                "관리자 티어 변경: " + oldTier + " → " + tier);
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
                            // PaymentLog는 PolarWebhookService에서 기록 (중복 방지)
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
        // PaymentLog는 PolarWebhookService에서 기록 (중복 방지)
    }

    // ── 구독 취소 (관리자 전용) ──

    /**
     * 구독 취소 요청 (기간 종료 시 FREE로 전환). 관리자 전용 API에서 호출.
     *
     * @param userId 취소할 사용자 ID
     * @param polarApiClient Polar API 클라이언트
     * @return 취소 응답 DTO
     */
    @Transactional
    public CancelSubscriptionResponse cancelSubscription(
            Long userId, PolarApiClient polarApiClient) {
        Subscription sub = getOrCreateSubscription(userId);

        if (sub.getTier() == SubscriptionTier.FREE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (sub.getCancelAtPeriodEnd()) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_CANCELED);
        }
        if (sub.getPolarSubscriptionId() == null) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        try {
            polarApiClient.cancelSubscription(sub.getPolarSubscriptionId());
        } catch (Exception e) {
            log.error("Polar 구독 취소 실패: userId={}", userId, e);
            throw new BusinessException(ErrorCode.CANCELLATION_FAILED);
        }

        sub.markCancelAtPeriodEnd(true);
        subscriptionRepository.save(sub);

        paymentLogService.log(
                userId,
                sub.getId(),
                PaymentEventType.CANCELLATION_REQUESTED,
                sub.getTier(),
                sub.getPolarSubscriptionId(),
                null,
                sub.getPolarCustomerId(),
                "관리자 구독 취소 요청",
                null);

        return CancelSubscriptionResponse.builder()
                .message("구독이 취소되었습니다. 현재 기간이 끝나면 Free로 전환됩니다.")
                .cancelAtPeriodEnd(true)
                .subscriptionEndDate(
                        sub.getSubscriptionEndDate() != null
                                ? sub.getSubscriptionEndDate().toString()
                                : null)
                .build();
    }

    // ── Polar.sh 구독 동기화 (수동) ──

    /**
     * Polar에서 사용자의 기존 구독 정보를 조회하여 로컬 DB에 동기화. 웹훅 누락 시 수동 복구용.
     *
     * @param userId 현재 로그인 사용자 ID
     * @param polarApiClient Polar API 클라이언트
     * @param polarProperties Polar 설정
     * @return 동기화된 사용량 응답 (tier가 업데이트된 상태)
     */
    @Transactional
    public UsageResponse syncSubscriptionFromPolar(
            Long userId,
            PolarApiClient polarApiClient,
            com.dreamtoon.global.config.PolarProperties polarProperties) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        try {
            com.fasterxml.jackson.databind.JsonNode polarSub = null;
            String polarCustomerId = null;

            // 1) Polar에서 이메일로 고객 조회
            com.fasterxml.jackson.databind.JsonNode customersResult =
                    polarApiClient.searchCustomersByEmail(user.getEmail());
            com.fasterxml.jackson.databind.JsonNode items = customersResult.path("items");

            if (items.isArray() && !items.isEmpty()) {
                polarCustomerId = items.get(0).path("id").asText();

                // 2) 해당 고객의 활성 구독 조회
                com.fasterxml.jackson.databind.JsonNode subsResult =
                        polarApiClient.listSubscriptions(polarCustomerId);
                com.fasterxml.jackson.databind.JsonNode subItems = subsResult.path("items");

                if (subItems.isArray() && !subItems.isEmpty()) {
                    polarSub = subItems.get(0);
                }
            }

            // 3) 이메일로 못 찾은 경우 → metadata.user_id 기반 fallback
            //    (카카오 소셜 로그인 등으로 로컬 이메일과 결제 이메일이 다를 수 있음)
            if (polarSub == null) {
                log.info(
                        "[Sync] Email lookup failed for {}. Trying metadata.user_id fallback...",
                        user.getEmail());
                com.fasterxml.jackson.databind.JsonNode allSubs =
                        polarApiClient.listAllActiveSubscriptions();
                com.fasterxml.jackson.databind.JsonNode allItems = allSubs.path("items");

                if (allItems.isArray()) {
                    String userIdStr = String.valueOf(userId);
                    for (com.fasterxml.jackson.databind.JsonNode sub : allItems) {
                        String metaUserId = sub.path("metadata").path("user_id").asText("");
                        if (userIdStr.equals(metaUserId)) {
                            polarSub = sub;
                            polarCustomerId = sub.path("customer_id").asText(null);
                            if (polarCustomerId == null || polarCustomerId.isEmpty()) {
                                polarCustomerId = sub.path("customer").path("id").asText(null);
                            }
                            log.info(
                                    "[Sync] Found subscription via metadata.user_id={}: subId={}",
                                    userIdStr,
                                    sub.path("id").asText());
                            break;
                        }
                    }
                }
            }

            if (polarSub == null) {
                log.info("[Sync] No Polar subscription found for userId={}", userId);
                return getUsage(userId);
            }

            // 4) 활성 구독의 정보로 로컬 DB 동기화
            String polarSubId = polarSub.path("id").asText();
            String productId = polarSub.path("product_id").asText();
            if (productId.isEmpty()) {
                productId = polarSub.path("product").path("id").asText();
            }
            String periodEnd = polarSub.path("current_period_end").asText();
            boolean cancelAtPeriodEnd = polarSub.path("cancel_at_period_end").asBoolean(false);

            SubscriptionTier tier = resolveTierFromProductId(productId, polarProperties);
            java.time.LocalDate endDate = parsePolarDate(periodEnd);

            Subscription sub = getOrCreateSubscription(userId);
            SubscriptionTier oldTier = sub.getTier();
            sub.activateSubscription(polarSubId, polarCustomerId, tier, endDate);
            sub.markCancelAtPeriodEnd(cancelAtPeriodEnd);
            subscriptionRepository.save(sub);

            log.info(
                    "[Sync] Synced Polar subscription for userId={}: {} → {}",
                    userId,
                    oldTier,
                    tier);
            paymentLogService.log(
                    userId,
                    sub.getId(),
                    PaymentEventType.SUBSCRIPTION_UPDATED,
                    tier,
                    "수동 동기화: " + oldTier + " → " + tier);

            long libraryCount = dreamRepository.countLibraryByUserId(userId);
            long favoriteCount = dreamRepository.countFavoritesByUserId(userId);
            return UsageResponse.from(sub, libraryCount, favoriteCount);

        } catch (Exception e) {
            log.error("[Sync] Polar 구독 동기화 실패: userId={}", userId, e);
            return getUsage(userId);
        }
    }

    // ── Polar.sh 결제 연동 ──

    /**
     * 결제 체크아웃 URL 생성. Polar에서 "이미 구독 중" 에러 발생 시 자동으로 동기화 시도.
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
            PolarApiClient polarApiClient,
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
            paymentLogService.log(
                    userId,
                    null,
                    PaymentEventType.CHECKOUT_CREATED,
                    tier,
                    "체크아웃 생성: " + tier.name());
            return com.dreamtoon.domain.subscription.dto.CheckoutResponse.builder()
                    .checkoutId(result.path("id").asText())
                    .checkoutUrl(result.path("url").asText())
                    .tier(tier.name())
                    .build();
        } catch (Exception e) {
            // "이미 구독 중" 에러인 경우 Polar에서 기존 구독 정보를 가져와서 동기화
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("already") || errorMsg.contains("active subscription")) {
                log.warn("[Checkout] 이미 구독 중인 사용자, 동기화 시도: userId={}", userId);
                syncSubscriptionFromPolar(userId, polarApiClient, polarProperties);
                throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
            }
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

    // ── 헬퍼 ──

    private SubscriptionTier resolveTierFromProductId(
            String productId, com.dreamtoon.global.config.PolarProperties polarProperties) {
        com.dreamtoon.global.config.PolarProperties.Products products =
                polarProperties.getProducts();
        if (productId.equals(products.getPlus())) return SubscriptionTier.PLUS;
        if (productId.equals(products.getPro())) return SubscriptionTier.PRO;
        if (productId.equals(products.getUltra())) return SubscriptionTier.ULTRA;
        log.warn("[Sync] Unknown product ID: {}, defaulting to FREE", productId);
        return SubscriptionTier.FREE;
    }

    private java.time.LocalDate parsePolarDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return null;
        try {
            return java.time.Instant.parse(isoDate).atZone(java.time.ZoneOffset.UTC).toLocalDate();
        } catch (Exception e) {
            log.warn("[Sync] Failed to parse date: {}", isoDate);
            return null;
        }
    }
}
