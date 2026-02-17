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

@Tag(
        name = "Subscriptions",
        description =
                "**구독·사용량** 관련 API입니다. 현재 사용자의 구독 등급, 꿈 생성/저장 사용 횟수, "
                        + "잔여 할당량 등을 조회할 수 있습니다. **Authorization: Bearer {accessToken}** 필요. "
                        + "(Users의 GET /me/permissions 와 용도가 겹칠 수 있음)")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "사용량 조회",
            description =
                    "**구독 티어**, **이번 달 사용량**(꿈 생성 횟수, 저장 횟수 등), **잔여 할당량**을 조회합니다. "
                            + "프리미엄/제한 안내, 업그레이드 유도 UI 등에서 사용하세요.")
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<UsageResponse>> getUsage(
            @AuthenticationPrincipal Long userId) {
        UsageResponse response = subscriptionService.getUsage(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
