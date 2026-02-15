package com.dreamtoon.domain.user.controller;

import com.dreamtoon.domain.user.dto.TokenResponse;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import com.dreamtoon.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

        private final JwtTokenProvider jwtTokenProvider;

        @Operation(
                        summary = "토큰 갱신",
                        description =
                                        "Refresh Token을 사용하여 새로운 Access Token을 발급합니다. "
                                                        + "Refresh Token은 Cookie 또는 요청 본문에서 가져옵니다.")
        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<TokenResponse>> refresh(
                        HttpServletRequest request, @RequestBody(required = false) RefreshTokenRequest body) {
                // 1. 쿠키에서 Refresh Token 찾기
                String refreshToken = null;
                if (request.getCookies() != null) {
                        refreshToken =
                                        Arrays.stream(request.getCookies())
                                                        .filter(cookie -> "refreshToken".equals(cookie.getName()))
                                                        .findFirst()
                                                        .map(Cookie::getValue)
                                                        .orElse(null);
                }

                // 2. 쿠키에 없으면 요청 본문에서 찾기 (쿠키 차단 fallback)
                if (refreshToken == null && body != null) {
                        refreshToken = body.getRefreshToken();
                }

                if (refreshToken == null || refreshToken.isBlank()) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("Refresh Token이 필요합니다."));
                }

                // 3. Refresh Token 검증
                if (!jwtTokenProvider.validateToken(refreshToken)) {
                        return ResponseEntity.status(401).body(ApiResponse.error("Refresh Token이 만료되었습니다."));
                }

                // 4. 새 Access Token 발급
                Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                // 실제로는 DB에서 사용자 정보 조회 필요
                String newAccessToken =
                                jwtTokenProvider.createAccessToken(
                                                userId,
                                                "", // email (DB 조회 필요)
                                                "ROLE_USER");

                TokenResponse response = TokenResponse.of(newAccessToken, refreshToken, 3600L);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "로그아웃", description = "로그아웃 처리 (클라이언트에서 토큰 삭제)")
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout() {
                // 실제로는 Refresh Token을 블랙리스트에 추가하는 로직 필요 (Redis 등)
                return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
        }

        // DTO
        public record RefreshTokenRequest(String refreshToken) {
                public String getRefreshToken() {
                        return refreshToken;
                }
        }
}
