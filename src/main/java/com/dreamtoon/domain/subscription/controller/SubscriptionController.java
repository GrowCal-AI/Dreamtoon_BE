package com.dreamtoon.domain.subscription.controller;

import com.dreamtoon.domain.subscription.dto.UsageResponse;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscriptions", description = "구독 관리 API")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

        private final SubscriptionService subscriptionService;

        @Operation(summary = "사용량 조회", description = "현재 구독 티어 및 생성/저장 사용량을 조회합니다.")
        @GetMapping("/usage")
        public ResponseEntity<ApiResponse<UsageResponse>> getUsage(
                        @AuthenticationPrincipal Long userId) {
                UsageResponse response = subscriptionService.getUsage(userId);
                return ResponseEntity.ok(ApiResponse.success(response));
        }
}
