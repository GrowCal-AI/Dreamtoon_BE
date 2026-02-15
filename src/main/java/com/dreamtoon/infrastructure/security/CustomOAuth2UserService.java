package com.dreamtoon.infrastructure.security;

import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

        private final UserRepository userRepository;

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                OAuth2User oAuth2User = super.loadUser(userRequest);

                String registrationId = userRequest.getClientRegistration().getRegistrationId();
                Map<String, Object> attributes = oAuth2User.getAttributes();

                OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, attributes);

                User user = saveOrUpdate(oAuthAttributes);

                return new CustomOAuth2User(
                                user.getId(),
                                user.getEmail(),
                                user.getNickname(),
                                user.getSocialProvider().name(),
                                user.getSocialId(),
                                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())),
                                attributes);
        }

        private User saveOrUpdate(OAuthAttributes attributes) {
                User user =
                                userRepository
                                                .findBySocialProviderAndSocialId(
                                                                attributes.getProvider(), attributes.getProviderId())
                                                .orElseGet(() -> attributes.toEntity());

                return userRepository.save(user);
        }
}
