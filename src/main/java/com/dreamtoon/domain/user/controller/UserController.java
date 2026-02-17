package com.dreamtoon.domain.user.controller;

import com.dreamtoon.domain.subscription.dto.UsageResponse;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.domain.user.dto.UserResponse;
import com.dreamtoon.domain.user.service.UserService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "닉네임 변경", description = "사용자의 닉네임을 변경합니다.")
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal Long userId, @RequestParam String nickname) {
        UserResponse response = userService.updateNickname(userId, nickname);
        return ResponseEntity.ok(ApiResponse.success("닉네임이 변경되었습니다.", response));
    }

    @Operation(summary = "권한 및 사용량 조회", description = "현재 사용자의 구독 등급, 사용량, 잔여 할당량 및 권한 정보를 조회합니다.")
    @GetMapping("/me/permissions")
    public ResponseEntity<ApiResponse<UsageResponse>> getMyPermissions(
            @AuthenticationPrincipal Long userId) {
        UsageResponse response = subscriptionService.getUsage(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
