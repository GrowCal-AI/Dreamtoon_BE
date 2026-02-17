package com.dreamtoon.domain.user.controller;

import com.dreamtoon.domain.subscription.dto.UsageResponse;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.domain.user.dto.UserResponse;
import com.dreamtoon.domain.user.service.UserService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Users",
        description =
                "**로그인한 사용자**의 프로필·닉네임·권한/사용량을 다룹니다. "
                        + "모든 API에 **Authorization: Bearer {accessToken}** 헤더가 필요합니다.")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "내 정보 조회",
            description =
                    "현재 **JWT로 로그인한 사용자**의 정보를 조회합니다. "
                            + "이메일, 닉네임, 소셜 제공자(Google/Kakao) 등 프로필 표시용으로 사용하세요.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "닉네임 변경",
            description =
                    "로그인한 사용자의 **닉네임**을 변경합니다. 쿼리 파라미터 `nickname`에 새 닉네임을 넣어 보내면 됩니다. "
                            + "변경된 사용자 정보가 응답에 포함됩니다.")
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "새 닉네임 (필수)") @RequestParam String nickname) {
        UserResponse response = userService.updateNickname(userId, nickname);
        return ResponseEntity.ok(ApiResponse.success("닉네임이 변경되었습니다.", response));
    }

    @Operation(
            summary = "권한 및 사용량 조회",
            description =
                    "현재 사용자의 **구독 티어**, **사용량**(꿈 생성/저장 횟수 등), **잔여 할당량**, **권한** 정보를 한 번에 조회합니다. "
                            + "프리미엄/제한 안내 UI나 설정 화면에서 호출하면 됩니다. (Subscriptions의 GET /usage 와 동일한 용도)")
    @GetMapping("/me/permissions")
    public ResponseEntity<ApiResponse<UsageResponse>> getMyPermissions(
            @AuthenticationPrincipal Long userId) {
        UsageResponse response = subscriptionService.getUsage(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
