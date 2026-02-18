package com.dreamtoon.infrastructure.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * OAuth2AuthenticationSuccessHandler 단위 테스트
 *
 * <p>
 * OAuth2 로그인 성공 시 JWT 발급 및 HTML 리다이렉트 응답을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private OAuth2AuthenticationSuccessHandler successHandler;

    private static final String MOCK_ACCESS_TOKEN = "mock-access-token";
    private static final String MOCK_REFRESH_TOKEN = "mock-refresh-token";
    private static final String FRONTEND_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        successHandler = new OAuth2AuthenticationSuccessHandler(jwtTokenProvider);
        ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);
    }

    @Test
    @DisplayName("로그인 성공 시 Access Token을 쿼리 파라미터로 포함하여 리다이렉트")
    void onAuthenticationSuccess_issuesTokensAndRedirects() throws Exception {
        when(jwtTokenProvider.createAccessToken(1L, "user@example.com", "ROLE_USER"))
                .thenReturn(MOCK_ACCESS_TOKEN);
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn(MOCK_REFRESH_TOKEN);

        CustomOAuth2User customUser = new CustomOAuth2User(
                1L,
                "user@example.com",
                "테스트유저",
                "KAKAO",
                "kakao-123",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                Collections.emptyMap());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customUser);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Verify Tokens Created
        verify(jwtTokenProvider).createAccessToken(1L, "user@example.com", "ROLE_USER");
        verify(jwtTokenProvider).createRefreshToken(1L);

        // Verify Redirect
        String redirectedUrl = response.getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).startsWith(FRONTEND_URL + "/oauth/callback");
        assertThat(redirectedUrl).contains("accessToken=" + MOCK_ACCESS_TOKEN);

        // Verify Cookie
        assertThat(response.getCookie("refreshToken")).isNotNull();
        assertThat(response.getCookie("refreshToken").getValue()).isEqualTo(MOCK_REFRESH_TOKEN);
        assertThat(response.getCookie("refreshToken").isHttpOnly()).isTrue();
    }

    @Test
    @DisplayName("response가 이미 commit된 경우 JWT 발급 및 쓰기 스킵")
    void onAuthenticationSuccess_whenCommitted_skipsProcessing() throws Exception {
        // committed 시 handler가 early return하므로 principal 사용 안 함 → 스텁 불필요
        Authentication authentication = mock(Authentication.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verifyNoInteractions(jwtTokenProvider);
        assertThat(response.getContentAsString()).isEmpty();
    }
}
