package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.InputMethod;
import com.dreamtoon.domain.dream.entity.StylePreset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 꿈 생성 요청 DTO
 *
 * <p>사용자로부터 꿈 내용을 입력받아 웹툰을 생성하기 위한 요청 객체입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateDreamRequest {

    /** 꿈 제목 */
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    /** 꿈 내용 */
    @NotBlank(message = "꿈 내용을 입력해주세요.")
    private String content;

    /** 웹툰 스타일 (필수) */
    @NotNull(message = "스타일을 선택해주세요.")
    private StylePreset style;

    /** 등장인물 목록 (선택) */
    private List<String> characters;

    /** 장소 목록 (선택) */
    private List<String> location;

    /** 주요 감정 (선택) */
    private EmotionType mainEmotion;

    /** 마지막 장면 설명 (선택) */
    private String lastScene;

    /** 입력 방식 (기본값: TEXT) */
    @Builder.Default private InputMethod inputMethod = InputMethod.TEXT;
}
