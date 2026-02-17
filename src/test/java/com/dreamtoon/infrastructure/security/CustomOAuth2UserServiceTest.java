package com.dreamtoon.infrastructure.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.dreamtoon.domain.user.entity.Role;
import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * CustomOAuth2UserService 단위 테스트
 *
 * <p>카카오/구글 OAuth2 콜백 후 loadUser() 동작을 검증합니다. 실제 OAuth2 인증 서버 호출은 하지 않고, loadOAuth2User()를 스텁하여
 * attributes만 주입한 뒤 saveOrUpdate 및 CustomOAuth2User 반환을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock private UserRepository userRepository;

    @Spy @InjectMocks private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("카카오 최초 로그인 → 신규 User 저장 후 CustomOAuth2User 반환")
    void loadUser_kakao_firstLogin() throws Exception {
        Map<String, Object> kakaoAttributes =
                Map.of(
                        "id",
                        12345678L,
                        "kakao_account",
                        Map.of(
                                "email",
                                "kakao@example.com",
                                "profile",
                                Map.of("nickname", "카카오유저")));

        when(userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, "12345678"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        inv -> {
                            User u = inv.getArgument(0);
                            ReflectionTestUtils.setField(u, "id", 1L);
                            return u;
                        });

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(kakaoAttributes);

        doReturn(mockOAuth2User)
                .when(customOAuth2UserService)
                .loadOAuth2User(any(OAuth2UserRequest.class));

        OAuth2UserRequest request = createOAuth2UserRequest("kakao");
        OAuth2User result = customOAuth2UserService.loadUser(request);

        assertThat(result).isInstanceOf(CustomOAuth2User.class);
        CustomOAuth2User custom = (CustomOAuth2User) result;
        assertThat(custom.getUserId()).isEqualTo(1L);
        assertThat(custom.getEmail()).isEqualTo("kakao@example.com");
        assertThat(custom.getNickname()).isEqualTo("카카오유저");
        assertThat(custom.getProvider()).isEqualTo("KAKAO");
        assertThat(custom.getProviderId()).isEqualTo("12345678");

        verify(userRepository).findBySocialProviderAndSocialId(SocialProvider.KAKAO, "12345678");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("카카오 재로그인 → 기존 User 반환 (save 시 기존 엔티티)")
    void loadUser_kakao_reLogin() throws Exception {
        Map<String, Object> kakaoAttributes =
                Map.of(
                        "id",
                        999L,
                        "kakao_account",
                        Map.of(
                                "email",
                                "existing@kakao.com",
                                "profile",
                                Map.of("nickname", "기존유저")));

        User existingUser =
                User.builder()
                        .email("existing@kakao.com")
                        .nickname("기존유저")
                        .socialProvider(SocialProvider.KAKAO)
                        .socialId("999")
                        .role(Role.ROLE_USER)
                        .build();
        ReflectionTestUtils.setField(existingUser, "id", 42L);

        when(userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, "999"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(kakaoAttributes);
        doReturn(mockOAuth2User)
                .when(customOAuth2UserService)
                .loadOAuth2User(any(OAuth2UserRequest.class));

        OAuth2UserRequest request = createOAuth2UserRequest("kakao");
        OAuth2User result = customOAuth2UserService.loadUser(request);

        assertThat(result).isInstanceOf(CustomOAuth2User.class);
        assertThat(((CustomOAuth2User) result).getUserId()).isEqualTo(42L);
        assertThat(((CustomOAuth2User) result).getEmail()).isEqualTo("existing@kakao.com");
        verify(userRepository).save(existingUser);
    }

    private static OAuth2UserRequest createOAuth2UserRequest(String registrationId) {
        ClientRegistration registration =
                ClientRegistration.withRegistrationId(registrationId)
                        .clientId("test-client")
                        .clientSecret("secret")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("http://localhost/callback")
                        .authorizationUri("https://auth.example.com/authorize")
                        .tokenUri("https://auth.example.com/token")
                        .userInfoUri("https://auth.example.com/userinfo")
                        .userNameAttributeName("kakao".equals(registrationId) ? "id" : "sub")
                        .build();
        OAuth2AccessToken token =
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER, "access-token", null, null);
        return new OAuth2UserRequest(registration, token);
    }
}
