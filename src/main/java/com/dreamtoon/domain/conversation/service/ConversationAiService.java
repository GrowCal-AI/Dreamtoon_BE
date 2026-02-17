package com.dreamtoon.domain.conversation.service;

import com.dreamtoon.domain.conversation.entity.ConversationPhase;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.infrastructure.ai.OpenAiClient;
import com.dreamtoon.infrastructure.ai.prompt.ConversationAiPrompt;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 대화형 AI 응답 생성 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationAiService {

    private final OpenAiClient openAiClient;

    /**
     * 환영 메시지 생성
     *
     * @return AI 생성 환영 메시지
     */
    public String generateWelcomeMessage() {
        try {
            String prompt = ConversationAiPrompt.generateWelcomeMessage();
            return callGpt(prompt);
        } catch (Exception e) {
            log.error("Failed to generate welcome message, using fallback", e);
            return "안녕하세요! 오늘 꿈에서 어떤 감정을 느끼셨나요? 아래에서 가장 가까운 감정을 선택해주세요.";
        }
    }

    /**
     * 감정 해석 생성
     *
     * @param emotions 선택된 감정과 강도
     * @param dominantEmotion 주요 감정
     * @return AI 생성 감정 해석
     */
    public String generateEmotionInterpretation(
            Map<EmotionType, Integer> emotions, EmotionType dominantEmotion) {
        try {
            String prompt =
                    ConversationAiPrompt.generateEmotionInterpretation(emotions, dominantEmotion);
            return callGpt(prompt);
        } catch (Exception e) {
            log.error("Failed to generate emotion interpretation, using fallback", e);
            return generateFallbackEmotionInterpretation(dominantEmotion, emotions);
        }
    }

    /**
     * 스트레스 평가 안내 메시지 생성
     *
     * @return AI 생성 안내 메시지
     */
    public String generateStressAssessmentIntro() {
        try {
            String prompt = ConversationAiPrompt.generateStressAssessmentIntro();
            return callGpt(prompt);
        } catch (Exception e) {
            log.error("Failed to generate stress assessment intro, using fallback", e);
            return "감정 분석이 완료되었습니다. 이제 현실에서 겪고 계신 스트레스를 평가해보겠습니다. 아래 항목별로 점수를 입력해주세요.";
        }
    }

    /**
     * 스트레스 권장사항 생성
     *
     * @param totalStressIndex 총 스트레스 지수
     * @param topStressors 주요 스트레스 요인
     * @param sleepQuality 수면 품질
     * @return AI 생성 권장사항
     */
    public String generateStressRecommendation(
            int totalStressIndex, List<String> topStressors, int sleepQuality) {
        try {
            String stressorsStr = String.join(", ", topStressors);
            String prompt =
                    ConversationAiPrompt.generateStressRecommendation(
                            totalStressIndex, stressorsStr, sleepQuality);
            return callGpt(prompt);
        } catch (Exception e) {
            log.error("Failed to generate stress recommendation, using fallback", e);
            return generateFallbackStressRecommendation(totalStressIndex, topStressors);
        }
    }

    /**
     * 일반 메시지 응답 생성
     *
     * @param userMessage 사용자 메시지
     * @param currentPhase 현재 대화 단계
     * @return AI 생성 응답
     */
    public String generateGeneralResponse(String userMessage, ConversationPhase currentPhase) {
        try {
            String prompt = ConversationAiPrompt.generateGeneralResponse(userMessage, currentPhase);
            return callGpt(prompt);
        } catch (Exception e) {
            log.error("Failed to generate general response, using fallback", e);
            return "메시지를 받았습니다. 계속 진행해주세요.";
        }
    }

    // === 내부 메서드 ===

    /** GPT-4o 호출 */
    private String callGpt(String prompt) {
        String response = openAiClient.analyzeWithGpt(prompt);
        log.debug(
                "GPT-4o response generated, length: {} chars",
                response != null ? response.length() : 0);
        return response != null ? response.trim() : "";
    }

    /** Fallback: 규칙 기반 감정 해석 */
    private String generateFallbackEmotionInterpretation(
            EmotionType dominantEmotion, Map<EmotionType, Integer> emotions) {
        int intensity = emotions.get(dominantEmotion);
        String intensityLevel = intensity >= 70 ? "강하게" : intensity >= 40 ? "보통으로" : "약하게";

        String interpretation =
                switch (dominantEmotion) {
                    case JOY -> intensityLevel + " 기쁨을 느끼셨군요. 긍정적인 꿈이었네요!";
                    case ANXIETY -> intensityLevel + " 불안감을 느끼셨네요. 현실의 걱정이 반영된 것 같습니다.";
                    case ANGER -> intensityLevel + " 분노를 느끼셨군요. 해결되지 않은 갈등이 있으신가요?";
                    case SADNESS -> intensityLevel + " 슬픔을 느끼셨네요. 마음이 힘드셨던 것 같습니다.";
                    case DISCOMFORT -> intensityLevel + " 불편함을 느끼셨군요. 어떤 부분이 불편하셨나요?";
                    case PEACE -> intensityLevel + " 평온함을 느끼셨네요. 마음이 안정되어 있으신 것 같습니다.";
                };

        return interpretation + " 이제 현실 상황을 평가해보겠습니다.";
    }

    /** Fallback: 규칙 기반 스트레스 권장사항 */
    private String generateFallbackStressRecommendation(
            int totalStressIndex, List<String> topStressors) {
        if (totalStressIndex >= 70) {
            return "스트레스 수준이 높습니다. " + String.join("과 ", topStressors) + " 부분에서 휴식이 필요해 보입니다.";
        } else if (totalStressIndex >= 40) {
            return "보통 수준의 스트레스입니다. " + String.join("과 ", topStressors) + " 부분을 개선해보세요.";
        } else {
            return "스트레스 수준이 낮습니다. 현재 상태를 잘 유지하고 계시네요!";
        }
    }
}
