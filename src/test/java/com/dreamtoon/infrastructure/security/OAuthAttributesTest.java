package com.dreamtoon.infrastructure.security;

import static org.assertj.core.api.Assertions.*;

import com.dreamtoon.domain.user.entity.Role;
import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * OAuthAttributes 단위 테스트
 *
 * <p>카카오/구글 OAuth2 사용자 정보 파싱 로직을 검증합니다. 외부 API 호출 없이 attributes Map만으로 동작하므로 순수 단위 테스트로 작성합니다.
 */
class OAuthAttributesTest {

    @Nested
    @DisplayName("카카오(Kakao) attributes 파싱")
    class Kakao {

        @Test
        @DisplayName("정상 카카오 응답 → OAuthAttributes 매핑")
        void ofKakao_normal() {
            Map<String, Object> attributes =
                    Map.of(
                            "id",
                            12345678L,
                            "kakao_account",
                            Map.of(
                                    "email",
                                    "user@example.com",
                                    "profile",
                                    Map.of("nickname", "카카오닉네임")));

            OAuthAttributes result = OAuthAttributes.of("kakao", attributes);

            assertThat(result.getEmail()).isEqualTo("user@example.com");
            assertThat(result.getNickname()).isEqualTo("카카오닉네임");
            assertThat(result.getProvider()).isEqualTo(SocialProvider.KAKAO);
            assertThat(result.getProviderId()).isEqualTo("12345678");
        }

        @Test
        @DisplayName("카카오 이메일 없을 때 → id@kakao.com 폴백")
        void ofKakao_emailFallback() {
            Map<String, Object> attributes =
                    Map.of(
                            "id",
                            87654321L,
                            "kakao_account",
                            Map.of("profile", Map.of("nickname", "닉네임만")));

            OAuthAttributes result = OAuthAttributes.of("kakao", attributes);

            assertThat(result.getEmail()).isEqualTo("87654321@kakao.com");
            assertThat(result.getNickname()).isEqualTo("닉네임만");
            assertThat(result.getProviderId()).isEqualTo("87654321");
        }

        @Test
        @DisplayName("카카오 이메일 빈 문자열 → id@kakao.com 폴백")
        void ofKakao_blankEmailFallback() {
            Map<String, Object> attributes =
                    Map.of(
                            "id",
                            111L,
                            "kakao_account",
                            Map.of("email", "   ", "profile", Map.of("nickname", "테스트")));

            OAuthAttributes result = OAuthAttributes.of("kakao", attributes);

            assertThat(result.getEmail()).isEqualTo("111@kakao.com");
        }

        @Test
        @DisplayName("toEntity() → User 엔티티 생성 (ROLE_USER)")
        void toEntity_kakao() {
            OAuthAttributes attrs =
                    OAuthAttributes.builder()
                            .email("kakao@test.com")
                            .nickname("카카오유저")
                            .provider(SocialProvider.KAKAO)
                            .providerId("kakao-123")
                            .build();

            User user = attrs.toEntity();

            assertThat(user.getEmail()).isEqualTo("kakao@test.com");
            assertThat(user.getNickname()).isEqualTo("카카오유저");
            assertThat(user.getSocialProvider()).isEqualTo(SocialProvider.KAKAO);
            assertThat(user.getSocialId()).isEqualTo("kakao-123");
            assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        }
    }

    @Nested
    @DisplayName("구글(Google) attributes 파싱")
    class Google {

        @Test
        @DisplayName("정상 구글 응답 → OAuthAttributes 매핑")
        void ofGoogle_normal() {
            Map<String, Object> attributes =
                    Map.of(
                            "sub", "google-sub-id-123",
                            "email", "user@gmail.com",
                            "name", "구글닉네임");

            OAuthAttributes result = OAuthAttributes.of("google", attributes);

            assertThat(result.getEmail()).isEqualTo("user@gmail.com");
            assertThat(result.getNickname()).isEqualTo("구글닉네임");
            assertThat(result.getProvider()).isEqualTo(SocialProvider.GOOGLE);
            assertThat(result.getProviderId()).isEqualTo("google-sub-id-123");
        }

        @Test
        @DisplayName("toEntity() → User 엔티티 생성")
        void toEntity_google() {
            OAuthAttributes attrs =
                    OAuthAttributes.builder()
                            .email("google@test.com")
                            .nickname("구글유저")
                            .provider(SocialProvider.GOOGLE)
                            .providerId("google-456")
                            .build();

            User user = attrs.toEntity();

            assertThat(user.getEmail()).isEqualTo("google@test.com");
            assertThat(user.getSocialProvider()).isEqualTo(SocialProvider.GOOGLE);
            assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        }
    }

    @Nested
    @DisplayName("등록되지 않은 provider")
    class UnsupportedProvider {

        @Test
        @DisplayName("지원하지 않는 provider → IllegalArgumentException")
        void of_unsupported() {
            assertThatThrownBy(() -> OAuthAttributes.of("naver", Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported provider");
        }
    }
}
