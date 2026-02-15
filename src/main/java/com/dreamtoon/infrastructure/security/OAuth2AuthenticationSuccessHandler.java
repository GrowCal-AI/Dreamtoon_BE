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

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

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

        // 🍪 Hybrid 방식: 쿠키 시도 + Fallback
        // Refresh Token을 HTTP-Only Cookie로 설정 (차단되면 sessionStorage로 fallback)
        try {
            jakarta.servlet.http.Cookie refreshCookie =
                    new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false); // 로컬: false, 프로덕션: true
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(refreshCookie);
        } catch (Exception e) {
            log.warn("Failed to set cookie, will use fallback storage", e);
        }

        // HTML 페이지로 토큰 전달 (쿠키 차단 시 대체 방안 제공)
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter()
                .write(
                        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>로그인 중...</title>
            </head>
            <body>
                <script>
                    const accessToken = '%s';
                    const refreshToken = '%s';
                    const redirectUrl = '%s';

                    // 🍪 쿠키 지원 여부 확인
                    function isCookieEnabled() {
                        try {
                            document.cookie = 'cookietest=1';
                            const cookieEnabled = document.cookie.indexOf('cookietest=') !== -1;
                            document.cookie = 'cookietest=1; expires=Thu, 01-Jan-1970 00:00:01 GMT';
                            return cookieEnabled;
                        } catch (e) {
                            return false;
                        }
                    }

                    const cookieEnabled = isCookieEnabled();

                    // 팝업 로그인 방식
                    if (window.opener) {
                        window.opener.postMessage({
                            type: 'oauth2-login-success',
                            accessToken: accessToken,
                            refreshToken: cookieEnabled ? null : refreshToken,  // 쿠키 가능하면 전송 안함
                            cookieEnabled: cookieEnabled
                        }, redirectUrl);
                        window.close();
                    }
                    // 일반 리다이렉트 방식
                    else {
                        // Access Token: sessionStorage (페이지 새로고침 시 사라짐, 보안 향상)
                        sessionStorage.setItem('accessToken', accessToken);

                        // Refresh Token: 쿠키 차단 시에만 sessionStorage 사용
                        if (!cookieEnabled) {
                            console.warn('⚠️ 쿠키가 차단되어 있습니다. sessionStorage를 사용합니다.');
                            sessionStorage.setItem('refreshToken', refreshToken);
                            sessionStorage.setItem('useCookies', 'false');
                        } else {
                            sessionStorage.setItem('useCookies', 'true');
                        }

                        window.location.href = redirectUrl;
                    }
                </script>
                <noscript>
                    <p>JavaScript가 비활성화되어 있습니다. 로그인을 완료하려면 JavaScript를 활성화해주세요.</p>
                </noscript>
                <p>로그인 처리 중...</p>
            </body>
            </html>
            """
                                .formatted(accessToken, refreshToken, redirectUri));
    }
}
