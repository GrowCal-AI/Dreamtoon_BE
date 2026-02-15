package com.dreamtoon.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
        GOOGLE("구글"),
        KAKAO("카카오");

        private final String description;
}
