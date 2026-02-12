package com.dreamtoon.domain.dream.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StylePreset {
    ROMANCE("로맨스", "Webtoon style, romantic atmosphere, soft colors, shoujo manga aesthetic"),
    FANTASY("판타지", "Webtoon style, epic fantasy setting, vibrant colors, adventure theme"),
    HEALING("힐링", "Webtoon style, peaceful and calming, pastel colors, cozy atmosphere"),
    SD_REFRAME("SD 리프레임", "Webtoon style, surreal dreamlike quality, artistic interpretation");
    
    private final String description;
    private final String promptTemplate;
}
