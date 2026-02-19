package com.dreamtoon.global.config;

import com.dreamtoon.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 구독 월별 쿼터 자동 리셋 스케줄러.
 *
 * <p>매일 자정(UTC 기준)에 실행되며, 구독 갱신일이 오늘인 구독을 찾아 쿼터를 초기화한다. {@code subscriptionEndDate}가 오늘 이전인 구독은
 * FREE로 자동 다운그레이드.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionQuotaScheduler {

    private final SubscriptionService subscriptionService;

    /**
     * 매일 자정(서버 시간)에 실행.
     *
     * <p>cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetMonthlyQuotas() {
        log.info("[Scheduler] Starting monthly quota reset...");
        subscriptionService.resetAllDueQuotas();
    }
}
