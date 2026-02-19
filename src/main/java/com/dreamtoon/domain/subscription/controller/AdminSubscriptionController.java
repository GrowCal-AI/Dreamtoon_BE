package com.dreamtoon.domain.subscription.controller;

import com.dreamtoon.domain.subscription.entity.SubscriptionTier;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자/개발자 전용 구독 관리 API. ⚠️ 결제 모듈 연동 전까지 개발/테스트 목적으로만 사용. SecurityConfig에서 ROLE_ADMIN 또는 별도 보호 필요.
 */
@Tag(name = "Admin - Subscription", description = "관리자 구독 관리 (개발/테스트용)")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "구독 티어 강제 변경 (개발/테스트용)")
    @PatchMapping("/users/{userId}/subscription")
    public ResponseEntity<ApiResponse<Void>> forceSetTier(
            @PathVariable Long userId, @Valid @RequestBody ForceSetTierRequest request) {
        subscriptionService.forceSetTier(userId, request.getTier());
        return ResponseEntity.ok(
                ApiResponse.success("User " + userId + " tier updated to " + request.getTier()));
    }

    @Data
    public static class ForceSetTierRequest {
        @NotNull private SubscriptionTier tier;
    }
}
