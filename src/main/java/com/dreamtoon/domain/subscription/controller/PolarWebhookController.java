package com.dreamtoon.domain.subscription.controller;

import com.dreamtoon.domain.subscription.service.PolarWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Polar.sh Webhook 수신 엔드포인트 (인증 불필요 - 서명으로 검증) */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class PolarWebhookController {

    private final PolarWebhookService polarWebhookService;

    /** Polar.sh Webhook 수신. Security 설정에서 이 경로는 permitAll 처리 필요. */
    @PostMapping("/polar")
    public ResponseEntity<Void> handlePolarWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestBody byte[] rawBody) {

        // 1. 서명 검증
        if (!polarWebhookService.verifySignature(
                webhookId, webhookTimestamp, webhookSignature, rawBody)) {
            log.warn("[Polar Webhook] Invalid signature, rejecting request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 이벤트 처리
        try {
            polarWebhookService.dispatch(rawBody);
        } catch (Exception e) {
            log.error("[Polar Webhook] Processing failed", e);
            // Polar는 5xx 응답 시 재전송하므로, 500 반환으로 재시도 유도
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
