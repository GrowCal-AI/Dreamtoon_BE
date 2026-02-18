package com.dreamtoon.domain.user.controller;

import com.dreamtoon.domain.user.dto.TokenResponse;
import com.dreamtoon.domain.user.entity.Role;
import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import com.dreamtoon.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(
        name = "Auth",
        description =
                "**인증·토큰** 관련 API입니다. 로그인은 OAuth2(Google/Kakao)로 진행되며, 발급받은 **Access Token**을 다른"
                        + " API 호출 시 **Authorization: Bearer {accessToken}** 헤더에 넣어 사용합니다. 토큰"
                        + " 갱신(Refresh), 로그아웃, 개발용 테스트 로그인을 제공합니다.")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Operation(
            summary = "카카오 소셜 로그인",
            description =
                    "**카카오 OAuth2 로그인**을 시작합니다. 이 API를 호출하면 카카오 로그인 페이지로 리다이렉트됩니다. "
                            + "Swagger에서는 직접 테스트할 수 없으며, **브라우저에서 직접 접속**해야 합니다.\n\n"
                            + "로그인 URL: `{서버주소}/oauth2/authorization/kakao`\n\n"
                            + "로그인 성공 시 Access Token과 Refresh Token이 발급됩니다.")
    @GetMapping("/oauth2/kakao-info")
    public ResponseEntity<ApiResponse<String>> kakaoLoginInfo() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "브라우저에서 /oauth2/authorization/kakao 로 접속하세요.",
                        "/oauth2/authorization/kakao"));
    }

    @Operation(
            summary = "[DEV] 테스트 로그인",
            description =
                    "**개발/로컬 환경 전용.** 실제 OAuth 로그인 없이 `userId`만으로 Access·Refresh 토큰을 발급받습니다."
                            + " Swagger에서 API 테스트할 때: 이 API로 토큰 발급 → 상단 'Authorize'에서 Bearer"
                            + " {accessToken} 입력 후 다른 API 호출. 프로덕션(prod)에서는 이 API가 노출되지 않습니다.")
    @PostMapping("/test-login")
    @org.springframework.context.annotation.Profile({"local", "dev"})
    public ResponseEntity<ApiResponse<TestTokenResponse>> testLogin(
            @Parameter(description = "테스트할 사용자 ID (기본값 1)") @RequestParam(defaultValue = "1")
                    Long userId) {

        // DB에 테스트 사용자가 없으면 자동 생성
        User user =
                userRepository
                        .findById(userId)
                        .orElseGet(
                                () ->
                                        userRepository.save(
                                                User.builder()
                                                        .email("test@dreamtoon.com")
                                                        .nickname("테스트유저")
                                                        .socialProvider(SocialProvider.KAKAO)
                                                        .socialId("test-" + userId)
                                                        .role(Role.ROLE_USER)
                                                        .build()));

        String accessToken =
                jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), "ROLE_USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        TestTokenResponse response = new TestTokenResponse(accessToken, refreshToken);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "테스트 토큰이 발급되었습니다. Swagger의 'Authorize' 버튼을 눌러 Bearer 토큰을 입력하세요.",
                        response));
    }

    @Operation(
            summary = "토큰 갱신 (Refresh)",
            description =
                    "**Access Token 만료 시** Refresh Token으로 새 Access Token을 발급받습니다. Refresh Token 전달"
                            + " 방법: 1) **Cookie** `refreshToken` (OAuth 로그인 후 서버가 설정), 2) 쿠키가 없을 경우"
                            + " **요청 body**에 `{ \"refreshToken\": \"...\" }` 로 보냅니다. 성공 시 새"
                            + " accessToken과 만료 시간이 반환되며, 이후 API 호출에 새 Access Token을 사용하세요.")
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

    @Operation(
            summary = "로그아웃",
            description =
                    "로그아웃 처리 시 **프론트에서 호출**하는 API입니다. 서버는 성공 메시지를 반환하며, "
                            + "**클라이언트에서는 저장된 Access Token·Refresh Token(또는 쿠키)을 삭제**해야 합니다. "
                            + "선택적으로 이 API 호출 후 로그인 화면으로 이동하세요.")
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

    public record TestTokenResponse(String accessToken, String refreshToken) {}
}
