package com.dreamtoon.infrastructure.security;

import com.dreamtoon.domain.user.entity.Role;
import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    
    private String email;
    private String nickname;
    private SocialProvider provider;
    private String providerId;
    
    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
    
    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name"))
                .provider(SocialProvider.GOOGLE)
                .providerId((String) attributes.get("sub"))
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        // 이메일이 없으면 카카오ID@kakao.com 형식으로 생성
        String email = (String) kakaoAccount.get("email");
        if (email == null || email.isBlank()) {
            email = attributes.get("id") + "@kakao.com";
        }
        
        return OAuthAttributes.builder()
                .email(email)
                .nickname((String) profile.get("nickname"))
                .provider(SocialProvider.KAKAO)
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }
    
    public User toEntity() {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .socialProvider(provider)
                .socialId(providerId)
                .role(Role.ROLE_USER)
                .build();
    }
}
