package com.dreamtoon.domain.dream.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 꿈 웹툰 스타일 (FE DreamStyle과 1:1 매핑) */
@Getter
@RequiredArgsConstructor
public enum Genre {
    CUSTOM(
            "custom",
            "맞춤형",
            "best fitting webtoon style automatically chosen based on dream content, vivid colors,"
                    + " expressive characters, emotional atmosphere matching the dream mood"),
    ROMANCE(
            "romance",
            "로맨스",
            "romantic webtoon style, soft pastel colors, dreamy atmosphere, shoujo manga"
                    + " aesthetic"),
    SCHOOL(
            "school",
            "학원물",
            "school life webtoon style, bright colors, youthful energy, campus setting"),
    DARK_FANTASY(
            "dark-fantasy",
            "다크 판타지",
            "dark fantasy webtoon style, deep shadows, mystical elements, gothic atmosphere"),
    HEALING(
            "healing",
            "힐링",
            "healing webtoon style, warm colors, peaceful mood, cozy pastel atmosphere"),
    COMEDY(
            "comedy",
            "코미디",
            "comedy webtoon style, exaggerated expressions, bright vivid colors, humorous tone"),
    HORROR("horror", "호러", "horror webtoon style, dark colors, eerie atmosphere, thriller shadows"),
    PIXAR(
            "pixar",
            "픽사",
            "pixar 3D animation style, rounded characters, vibrant lighting, family-friendly"),
    GHIBLI(
            "ghibli",
            "지브리",
            "studio ghibli style, hand-drawn watercolor, soft nature, whimsical atmosphere"),
    CYBERPUNK(
            "cyberpunk",
            "사이버펑크",
            "cyberpunk neon style, futuristic cityscape, glowing lights, high-tech low-life"),
    CINEMATIC(
            "cinematic",
            "시네마틱",
            "cinematic film style, dramatic lighting, wide angle, movie poster composition"),
    VINTAGE(
            "vintage",
            "빈티지",
            "vintage retro style, sepia tones, film grain texture, nostalgic atmosphere");

    private final String code;
    private final String description;
    private final String promptTemplate;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static Genre fromCode(String code) {
        for (Genre genre : values()) {
            if (genre.code.equalsIgnoreCase(code) || genre.name().equalsIgnoreCase(code)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown style: " + code);
    }
}
