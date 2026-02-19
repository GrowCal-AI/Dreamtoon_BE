package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 비회원 꿈 미리보기 생성 요청 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestDreamCreateRequest {

    @NotBlank(message = "꿈 내용은 필수입니다")
    private String content;

    private EmotionType mainEmotion;

    @NotNull(message = "스타일 선택은 필수입니다")
    private Genre style;
}
