package com.dreamtoon.domain.dream.constants;

import com.dreamtoon.domain.dream.entity.EmotionType;
import java.util.EnumMap;
import java.util.Map;

/** 감정별 시스템 메시지 매핑 (Blueprint Section 11) */
public final class EmotionMessages {

    private static final Map<EmotionType, String> MESSAGES = new EnumMap<>(EmotionType.class);

    static {
        MESSAGES.put(EmotionType.JOY, "좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?");
        MESSAGES.put(EmotionType.ANXIETY, "불안한 꿈이셨군요. 어떤 부분이 가장 불안하셨나요?");
        MESSAGES.put(EmotionType.ANGER, "화가 나는 꿈이셨군요. 무엇이 가장 화나셨나요?");
        MESSAGES.put(EmotionType.SADNESS, "슬픈 꿈이셨군요. 어떤 점이 가장 슬프셨나요?");
        MESSAGES.put(EmotionType.DISCOMFORT, "불편한 꿈이셨군요. 어떤 부분이 불편하셨나요?");
        MESSAGES.put(EmotionType.PEACE, "평온한 꿈이셨군요. 어떤 느낌이 드셨나요?");
    }

    private EmotionMessages() {}

    /** 감정에 해당하는 시스템 메시지 반환 */
    public static String getMessage(EmotionType emotion) {
        return MESSAGES.getOrDefault(emotion, "");
    }
}
