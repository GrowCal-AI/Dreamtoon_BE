package com.dreamtoon.domain.subscription.service;

import com.dreamtoon.domain.subscription.entity.PaymentEventType;
import com.dreamtoon.domain.subscription.entity.PaymentLog;
import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import com.dreamtoon.domain.subscription.repository.PaymentLogRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 결제 이벤트 로그 기록 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;

    /** 결제 이벤트 로그 기록. 새 트랜잭션에서 실행되어 호출자의 트랜잭션 실패와 독립. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            Long userId,
            Long subscriptionId,
            PaymentEventType eventType,
            SubscriptionTier tier,
            String polarSubscriptionId,
            String polarCheckoutId,
            String polarCustomerId,
            String description,
            Map<String, Object> metadata) {
        try {
            PaymentLog paymentLog =
                    PaymentLog.builder()
                            .userId(userId)
                            .subscriptionId(subscriptionId)
                            .eventType(eventType)
                            .tier(tier)
                            .polarSubscriptionId(polarSubscriptionId)
                            .polarCheckoutId(polarCheckoutId)
                            .polarCustomerId(polarCustomerId)
                            .description(description)
                            .metadata(metadata)
                            .build();
            paymentLogRepository.save(paymentLog);
            log.debug("[PaymentLog] {} - userId={}, tier={}", eventType, userId, tier);
        } catch (Exception e) {
            // 로깅 실패가 비즈니스 로직을 중단해서는 안 됨
            log.error(
                    "[PaymentLog] Failed to save payment log: eventType={}, userId={}",
                    eventType,
                    userId,
                    e);
        }
    }

    /** 간편 로깅 (Polar ID 없는 경우) */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            Long userId,
            Long subscriptionId,
            PaymentEventType eventType,
            SubscriptionTier tier,
            String description) {
        log(userId, subscriptionId, eventType, tier, null, null, null, description, null);
    }

    /** 사용자별 결제 로그 조회 (페이지네이션) */
    @Transactional(readOnly = true)
    public Page<PaymentLog> getLogsByUserId(Long userId, Pageable pageable) {
        return paymentLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
