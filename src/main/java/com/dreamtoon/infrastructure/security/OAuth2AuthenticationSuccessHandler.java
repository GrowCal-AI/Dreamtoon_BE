package com.dreamtoon.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken =
                jwtTokenProvider.createAccessToken(
                        oAuth2User.getUserId(),
                        oAuth2User.getEmail(),
                        oAuth2User.getAuthorities().iterator().next().getAuthority());

        String refreshToken = jwtTokenProvider.createRefreshToken(oAuth2User.getUserId());

        // 🍪 Hybrid 방식: Refresh Token은 보안 쿠키에 저장
        // Access Token은 리다이렉트 URL 쿼리 파라미터로 전달
        addRefreshTokenCookie(response, refreshToken);

        // 프론트엔드 리다이렉트 (Access Token 포함)
        String targetUrl =
                org.springframework.web.util.UriComponentsBuilder.fromUriString(
                                frontendUrl + "/oauth/callback")
                        .queryParam("accessToken", accessToken)
                        .build()
                        .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        // 배포 환경(HTTPS)에서는 SameSite=None; Secure 설정이 필요할 수 있음
        // 현재는 기본적인 HttpOnly 쿠키 설정
        jakarta.servlet.http.Cookie refreshCookie =
                new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // 로컬: false, 프로덕션: true (SSL 적용 시 true 권장)
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshCookie);
    }
}
