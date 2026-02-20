package com.dreamtoon.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * OAuth2 로그인 시작 시 redirect_uri 쿼리 파라미터를 쿠키에 저장하는 필터.
 *
 * <p>프론트엔드에서 소셜 로그인 URL에 redirect_uri를 붙이면, 인증 성공 후 해당 주소로 리다이렉트됩니다. 로컬 개발 환경에서 유용합니다.
 *
 * <p>사용 예시: /oauth2/authorization/kakao?redirect_uri=http://localhost:5173
 */
@Slf4j
@Component
public class OAuth2RedirectUriFilter extends OncePerRequestFilter {

    static final String REDIRECT_URI_COOKIE_NAME = "oauth_redirect_uri";

    private final List<String> allowedRedirectUris;

    // @Value는 YAML 리스트를 List<String>으로 직접 주입할 수 없으므로, 콤마 구분 문자열로 받아서 변환
    public OAuth2RedirectUriFilter(
            @Value("${app.oauth2.allowed-redirect-uris:}") String allowedRedirectUrisConfig) {
        this.allowedRedirectUris =
                allowedRedirectUrisConfig.isBlank()
                        ? List.of()
                        : Arrays.stream(allowedRedirectUrisConfig.split(","))
                                .map(String::trim)
                                .toList();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/oauth2/authorization/")) {
            String redirectUri = request.getParameter("redirect_uri");
            if (redirectUri != null) {
                if (allowedRedirectUris.contains(redirectUri)) {
                    Cookie cookie = new Cookie(REDIRECT_URI_COOKIE_NAME, redirectUri);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(300); // 5분 (OAuth 흐름 완료 시간 이내)
                    response.addCookie(cookie);
                    log.debug("OAuth redirect_uri 쿠키 저장: {}", redirectUri);
                } else {
                    log.warn("허용되지 않은 redirect_uri 요청 무시: {}", redirectUri);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
