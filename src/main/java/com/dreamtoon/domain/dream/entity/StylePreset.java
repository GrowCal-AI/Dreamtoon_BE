package com.dreamtoon.domain.dream.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StylePreset {
    ROMANCE("로맨스", "Webtoon style, romantic atmosphere, soft colors, shoujo manga aesthetic"),
    SCHOOL("학원물", "Webtoon style, school life setting, youthful energy, slice of life"),
    DARK_FANTASY(
            "다크 판타지", "Webtoon style, dark fantasy setting, dramatic lighting, intense atmosphere"),
    FANTASY("판타지", "Webtoon style, epic fantasy setting, vibrant colors, adventure theme"),
    HEALING("힐링", "Webtoon style, peaceful and calming, pastel colors, cozy atmosphere"),
    COMEDY(
            "코미디",
            "Webtoon style, humorous and lighthearted, exaggerated expressions, bright colors"),
    HORROR("호러", "Webtoon style, suspenseful and eerie, dark shadows, thriller atmosphere"),
    SD_REFRAME("SD 리프레임", "Webtoon style, surreal dreamlike quality, artistic interpretation");

    private final String description;
    private final String promptTemplate;
}
