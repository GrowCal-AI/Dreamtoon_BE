package com.dreamtoon.domain.dream.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 4컷 만화 장르 */
@Getter
@RequiredArgsConstructor
public enum Genre {
    ROMANCE(
            "로맨스",
            "romantic webtoon style, soft pastel colors, dreamy atmosphere, shoujo manga"
                    + " aesthetic"),
    FANTASY("판타지", "fantasy webtoon style, vibrant colors, magical elements, epic adventure theme"),
    HEALING("힐링", "healing webtoon style, warm colors, peaceful mood, cozy pastel atmosphere"),
    HORROR("호러", "horror webtoon style, dark colors, eerie atmosphere, thriller shadows");

    private final String description;
    private final String promptTemplate;
}
