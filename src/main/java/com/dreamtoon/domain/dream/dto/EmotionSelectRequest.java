package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 감정 선택 요청 (Step 2) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionSelectRequest {

    @NotNull(message = "주요 감정은 필수입니다")
    private EmotionType primaryEmotion;
}
