package com.dreamtoon.domain.dream.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 꿈 웹툰 스타일 필터 (FE 필터와 1:1 매핑) */
@Getter
@RequiredArgsConstructor
public enum Genre {

    // ── 스탠다드 필터 ──
    CUSTOM(
            "custom",
            "맞춤형",
            "맞춤형 (AI 자동 선택)",
            "best fitting webtoon style automatically chosen based on dream content, vivid colors,"
                    + " expressive characters, emotional atmosphere matching the dream mood",
            false),

    // ── 프리미엄 필터 (PRO 뱃지) ──
    GHIBLI(
            "ghibli",
            "지브리",
            "몽글몽글한 감성",
            "studio ghibli style, hand-drawn watercolor, soft nature backgrounds, whimsical"
                    + " atmosphere, gentle warm tones",
            true),
    MARVEL(
            "marvel",
            "마블",
            "히어로 코믹스 스타일",
            "american superhero comic style, bold ink outlines, halftone dot shading, dynamic"
                    + " action poses, dramatic lighting",
            true),
    LEGO(
            "lego",
            "레고",
            "귀여운 블록 세계",
            "toy brick figure style, blocky plastic characters, bright primary colors, smooth"
                    + " plastic texture, cheerful toy world",
            true),
    ANIMAL_CROSSING(
            "animal-crossing",
            "모동숲",
            "포근한 동물의 숲",
            "cute chibi village life style, soft pastel colors, rounded friendly characters,"
                    + " cozy nature setting, warm comfortable atmosphere",
            true),

    // ── 내부 확장 장르 (CUSTOM 모드에서 AI가 자동 선택) ──
    ROMANCE(
            "romance",
            "로맨스",
            "로맨틱한 감성",
            "romantic webtoon style, soft pastel colors, dreamy atmosphere, shoujo manga aesthetic",
            false),
    SCHOOL(
            "school",
            "학원물",
            "청춘 학원 스타일",
            "school life webtoon style, bright colors, youthful energy, campus setting",
            false),
    DARK_FANTASY(
            "dark-fantasy",
            "다크 판타지",
            "어둡고 신비로운",
            "dark fantasy webtoon style, deep shadows, mystical elements, gothic atmosphere",
            false),
    HEALING(
            "healing",
            "힐링",
            "따뜻하고 포근한",
            "healing webtoon style, warm colors, peaceful mood, cozy pastel atmosphere",
            false),
    COMEDY(
            "comedy",
            "코미디",
            "유쾌한 개그 스타일",
            "comedy webtoon style, exaggerated expressions, bright vivid colors, humorous tone",
            false),
    HORROR(
            "horror",
            "호러",
            "소름 돋는 공포",
            "horror webtoon style, dark colors, eerie atmosphere, thriller shadows",
            false),
    PIXAR(
            "pixar",
            "픽사",
            "3D 애니메이션 감성",
            "pixar 3D animation style, rounded characters, vibrant lighting, family-friendly",
            false),
    CYBERPUNK(
            "cyberpunk",
            "사이버펑크",
            "미래 네온 도시",
            "cyberpunk neon style, futuristic cityscape, glowing lights, high-tech low-life",
            false),
    CINEMATIC(
            "cinematic",
            "시네마틱",
            "영화 같은 연출",
            "cinematic film style, dramatic lighting, wide angle, movie poster composition",
            false),
    VINTAGE(
            "vintage",
            "빈티지",
            "감성 복고 스타일",
            "vintage retro style, sepia tones, film grain texture, nostalgic atmosphere",
            false);

    private final String code;
    private final String name;
    private final String description;
    private final String promptTemplate;
    private final boolean premium;

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

    /** UI에 노출되는 필터 목록 (스탠다드 + 프리미엄) */
    public static java.util.List<Genre> getUiFilters() {
        return java.util.Arrays.asList(CUSTOM, GHIBLI, MARVEL, LEGO, ANIMAL_CROSSING);
    }
}
