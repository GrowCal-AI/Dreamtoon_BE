package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.InputMethod;
import com.dreamtoon.domain.dream.entity.StylePreset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateDreamRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "꿈 내용을 입력해주세요.")
    private String content;

    @NotNull(message = "스타일을 선택해주세요.")
    private StylePreset style;

    private List<String> characters;

    private List<String> location;

    private EmotionType mainEmotion;

    private String lastScene;

    private InputMethod inputMethod = InputMethod.TEXT; // 기본값: 텍스트 입력
}
