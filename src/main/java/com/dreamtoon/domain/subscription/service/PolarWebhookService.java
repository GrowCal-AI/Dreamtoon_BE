package com.dreamtoon.domain.subscription.service;

import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.config.PolarProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Polar.sh Webhook 이벤트 처리 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolarWebhookService {

    private final PolarProperties polarProperties;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ── 서명 검증 (Standard Webhooks - HMAC-SHA256) ──

    /**
     * Standard Webhooks 스펙에 따라 HMAC-SHA256 서명 검증.
     *
     * @param webhookId Webhook 고유 ID (헤더: webhook-id)
     * @param webhookTimestamp Unix timestamp 문자열 (헤더: webhook-timestamp)
     * @param webhookSignature 서명값 (헤더: webhook-signature, "v1,<base64>" 형식)
     * @param rawBody 요청 본문 바이트
     * @return 검증 성공 여부
     */
    public boolean verifySignature(
            String webhookId, String webhookTimestamp, String webhookSignature, byte[] rawBody) {
        try {
            // 5분 타임스탬프 유효성 검사
            long timestamp = Long.parseLong(webhookTimestamp);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - timestamp) > 300) {
                log.warn("[Polar Webhook] Timestamp out of tolerance: {}", webhookTimestamp);
                return false;
            }

            // 서명 대상 문자열: "<webhookId>.<webhookTimestamp>.<body>"
            String signedContent =
                    webhookId
                            + "."
                            + webhookTimestamp
                            + "."
                            + new String(rawBody, StandardCharsets.UTF_8);

            // HMAC-SHA256 계산 (시크릿은 base64 디코딩)
            byte[] secretBytes = Base64.getDecoder().decode(polarProperties.getWebhookSecret());
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            String computed =
                    Base64.getEncoder()
                            .encodeToString(
                                    mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8)));

            // 헤더에서 서명 추출 (공백 구분, 여러 서명 가능)
            for (String sig : webhookSignature.split(" ")) {
                String[] parts = sig.split(",", 2);
                if (parts.length == 2 && "v1".equals(parts[0]) && computed.equals(parts[1])) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("[Polar Webhook] Signature verification error", e);
            return false;
        }
    }

    // ── 이벤트 디스패치 ──

    public void dispatch(byte[] rawBody) throws Exception {
        JsonNode event = objectMapper.readTree(rawBody);
        String eventType = event.path("type").asText();
        JsonNode data = event.path("data");

        log.info("[Polar Webhook] Event received: {}", eventType);

        switch (eventType) {
            case "subscription.created" -> handleSubscriptionCreated(data);
            case "subscription.updated", "subscription.active" -> handleSubscriptionUpdated(data);
            case "subscription.canceled" -> handleSubscriptionCanceled(data);
            case "subscription.revoked" -> handleSubscriptionRevoked(data);
            default -> log.debug("[Polar Webhook] Unhandled event: {}", eventType);
        }
    }

    // ── 이벤트 핸들러 ──

    private void handleSubscriptionCreated(JsonNode data) {
        String polarSubId = data.path("id").asText();
        String polarCustomerId = data.path("customer").path("id").asText();
        String customerEmail = data.path("customer").path("email").asText();
        String productId = data.path("product").path("id").asText();
        String currentPeriodEnd = data.path("current_period_end").asText();

        SubscriptionTier tier = resolveTier(productId);
        LocalDate endDate = parseDate(currentPeriodEnd);

        // email로 내부 사용자 조회
        userRepository
                .findByEmail(customerEmail)
                .ifPresentOrElse(
                        user ->
                                subscriptionService.activateNewSubscription(
                                        user.getId(), polarSubId, polarCustomerId, tier, endDate),
                        () ->
                                log.warn(
                                        "[Polar Webhook] User not found for email: {}",
                                        customerEmail));
    }

    private void handleSubscriptionUpdated(JsonNode data) {
        String polarSubId = data.path("id").asText();
        String polarCustomerId = data.path("customer").path("id").asText();
        String productId = data.path("product").path("id").asText();
        String currentPeriodEnd = data.path("current_period_end").asText();
        boolean cancelAtPeriodEnd = data.path("cancel_at_period_end").asBoolean(false);

        SubscriptionTier tier = resolveTier(productId);
        LocalDate endDate = parseDate(currentPeriodEnd);

        subscriptionService.syncFromPolar(
                polarSubId, polarCustomerId, tier, endDate, cancelAtPeriodEnd);
    }

    private void handleSubscriptionCanceled(JsonNode data) {
        // 취소는 기간 만료 시 FREE로 변경 (cancel_at_period_end=true)
        String polarSubId = data.path("id").asText();
        subscriptionService.syncFromPolar(
                polarSubId,
                data.path("customer").path("id").asText(),
                resolveTier(data.path("product").path("id").asText()),
                parseDate(data.path("current_period_end").asText()),
                true);
    }

    private void handleSubscriptionRevoked(JsonNode data) {
        // 즉시 FREE 다운그레이드
        String polarSubId = data.path("id").asText();
        subscriptionService.revokeFromPolar(polarSubId);
    }

    // ── 헬퍼 ──

    private SubscriptionTier resolveTier(String productId) {
        PolarProperties.Products products = polarProperties.getProducts();
        if (productId.equals(products.getPlus())) return SubscriptionTier.PLUS;
        if (productId.equals(products.getPro())) return SubscriptionTier.PRO;
        if (productId.equals(products.getUltra())) return SubscriptionTier.ULTRA;
        log.warn("[Polar Webhook] Unknown product ID: {}, defaulting to FREE", productId);
        return SubscriptionTier.FREE;
    }

    private LocalDate parseDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return null;
        try {
            return Instant.parse(isoDate).atZone(java.time.ZoneOffset.UTC).toLocalDate();
        } catch (Exception e) {
            log.warn("[Polar Webhook] Failed to parse date: {}", isoDate);
            return null;
        }
    }
}
