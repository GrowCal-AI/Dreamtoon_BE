package com.dreamtoon.domain.conversation.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionSummaryResponse {

        private EmotionType dominantEmotion;
        private Integer dominantIntensity;
        private String aiInterpretation;
}
