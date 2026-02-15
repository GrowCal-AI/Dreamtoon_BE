package com.dreamtoon.domain.conversation.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmotionSelectionRequest {

        @NotEmpty(message = "최소 1개 이상의 감정을 선택해야 합니다")
        private Map<EmotionType, @NotNull @Min(0) @Max(100) Integer> emotionIntensities;
}
