package com.dreamtoon.domain.subscription.controller;

import com.dreamtoon.domain.subscription.dto.CheckoutRequest;
import com.dreamtoon.domain.subscription.dto.CheckoutResponse;
import com.dreamtoon.domain.subscription.dto.CustomerPortalResponse;
import com.dreamtoon.domain.subscription.dto.PaymentLogResponse;
import com.dreamtoon.domain.subscription.dto.UsageResponse;
import com.dreamtoon.domain.subscription.service.PaymentLogService;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.global.common.dto.request.PageRequest;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import com.dreamtoon.global.common.dto.response.PageResponse;
import com.dreamtoon.global.config.PolarProperties;
import com.dreamtoon.infrastructure.payment.PolarApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Subscriptions",
        description =
                "**구독·사용량** 관련 API입니다. 현재 사용자의 구독 등급, 꿈 생성/저장 사용 횟수, "
                        + "잔여 할당량 등을 조회할 수 있습니다. **Authorization: Bearer {accessToken}** 필요.")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PaymentLogService paymentLogService;
    private final PolarApiClient polarApiClient;
    private final PolarProperties polarProperties;

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

    @Operation(
            summary = "결제 체크아웃 URL 생성",
            description =
                    "선택한 구독 티어(PLUS/PRO/ULTRA)의 **Polar.sh 결제 페이지 URL**을 생성합니다. "
                            + "응답의 `checkoutUrl`로 프론트엔드에서 리다이렉트하면 결제가 완료됩니다. "
                            + "결제 완료 후 Webhook을 통해 구독이 자동으로 활성화됩니다.")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> createCheckout(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response =
                subscriptionService.createCheckoutUrl(
                        userId, request.getTier(), polarApiClient, polarProperties);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Customer Portal URL 생성",
            description =
                    "Polar.sh **구독 관리 페이지** URL을 생성합니다. "
                            + "응답의 `portalUrl`로 리다이렉트하면 구독 변경·해지 등을 직접 관리할 수 있습니다. "
                            + "유료 구독자만 사용 가능합니다 (Polar Customer ID 필요).")
    @GetMapping("/portal")
    public ResponseEntity<ApiResponse<CustomerPortalResponse>> getCustomerPortal(
            @AuthenticationPrincipal Long userId) {
        CustomerPortalResponse response =
                subscriptionService.createCustomerPortalUrl(userId, polarApiClient);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "구독 동기화",
            description =
                    "Polar.sh에서 현재 사용자의 **기존 구독 정보를 조회하여 로컬 DB에 동기화**합니다. "
                            + "웹훅 누락으로 결제는 완료되었으나 구독 등급이 반영되지 않은 경우 호출하세요. "
                            + "동기화 후 최신 사용량 정보를 반환합니다.")
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<UsageResponse>> syncSubscription(
            @AuthenticationPrincipal Long userId) {
        UsageResponse response =
                subscriptionService.syncSubscriptionFromPolar(
                        userId, polarApiClient, polarProperties);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 이력 조회", description = "현재 사용자의 결제/구독 이벤트 이력을 페이지네이션으로 조회합니다.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<PaymentLogResponse>>> getPaymentHistory(
            @AuthenticationPrincipal Long userId, @ModelAttribute PageRequest pageRequest) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        PageResponse.of(
                                paymentLogService
                                        .getLogsByUserId(userId, pageRequest.toPageable())
                                        .map(PaymentLogResponse::from))));
    }
}
