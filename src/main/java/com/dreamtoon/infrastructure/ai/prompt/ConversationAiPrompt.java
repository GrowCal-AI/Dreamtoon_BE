package com.dreamtoon.infrastructure.ai.prompt;

import com.dreamtoon.domain.conversation.entity.ConversationPhase;
import com.dreamtoon.domain.dream.entity.EmotionType;
import java.util.Map;

/** 대화형 AI 프롬프트 생성 */
public class ConversationAiPrompt {

    /** 감정 수집 단계 - 초기 환영 메시지 생성 */
    public static String generateWelcomeMessage() {
        return """
                                당신은 드림툰의 친근하고 공감적인 AI 심리 상담사입니다.
                                사용자가 꿈을 기록하러 왔습니다.

                                다음 형식으로 환영 메시지를 작성하세요:
                                1. 따뜻한 인사 (1문장)
                                2. 꿈에서 느낀 감정을 선택하도록 유도 (1문장)
                                3. "아래의 감정 칩 중에서 선택해주세요" 안내 (1문장)

                                톤: 친근하고 따뜻하며, 전문적이되 격식을 차리지 않음
                                길이: 3문장 이내
                                """;
    }

    /** 감정 수집 단계 - 사용자가 선택한 감정에 대한 AI 해석 생성 */
    public static String generateEmotionInterpretation(
            Map<EmotionType, Integer> emotions, EmotionType dominantEmotion) {
        StringBuilder emotionList = new StringBuilder();
        emotions.forEach(
                (emotion, intensity) -> {
                    String emotionKr = getEmotionKoreanName(emotion);
                    emotionList.append(String.format("- %s: %d%%\n", emotionKr, intensity));
                });

        return String.format(
                """
                                당신은 드림툰의 AI 심리 상담사입니다.
                                사용자가 꿈에서 느낀 감정을 다음과 같이 선택했습니다:

                                %s
                                주요 감정: %s

                                이 감정 조합을 분석하여 다음을 포함한 응답을 생성하세요:
                                1. 감정 상태에 대한 공감 (1-2문장)
                                2. 주요 감정과 부차 감정의 관계 해석 (1문장)
                                3. 다음 단계(스트레스 평가)로 자연스럽게 연결 (1문장)

                                톤: 공감적이고 따뜻하며 긍정적
                                길이: 3-4문장
                                """,
                emotionList.toString(), getEmotionKoreanName(dominantEmotion));
    }

    /** 상황 분석 단계 - 스트레스 평가 안내 메시지 생성 */
    public static String generateStressAssessmentIntro() {
        return """
                                당신은 드림툰의 AI 심리 상담사입니다.
                                감정 분석을 마치고 이제 현실 상황 분석 단계로 넘어갑니다.

                                다음을 포함한 메시지를 생성하세요:
                                1. 감정 분석 완료에 대한 간단한 언급 (1문장)
                                2. 현실에서 겪는 스트레스 평가의 중요성 설명 (1-2문장)
                                3. 스트레스 평가를 시작하도록 유도 (1문장)

                                톤: 격려하는 듯하면서도 전문적
                                길이: 3-4문장
                                """;
    }

    /** 상황 분석 단계 - 스트레스 평가 결과 해석 */
    public static String generateStressRecommendation(
            int totalStressIndex, String topStressors, int sleepQuality) {
        String stressLevel = totalStressIndex >= 70 ? "높음" : totalStressIndex >= 40 ? "보통" : "낮음";
        String sleepLevel = sleepQuality >= 80 ? "양호" : sleepQuality >= 50 ? "보통" : "불량";

        return String.format(
                """
                                당신은 드림툰의 AI 심리 상담사입니다.
                                사용자의 스트레스 평가 결과:
                                - 총 스트레스 지수: %d/100 (%s)
                                - 주요 스트레스 요인: %s
                                - 수면 품질: %d/100 (%s)

                                다음을 포함한 권장사항을 생성하세요:
                                1. 스트레스 수준에 대한 객관적 평가 (1문장)
                                2. 주요 스트레스 요인에 대한 구체적 조언 (1-2문장)
                                3. 수면 품질 개선 제안 (1문장, 수면 품질이 낮은 경우에만)
                                4. 긍정적 격려와 다음 단계 안내 (1문장)

                                톤: 전문적이면서도 따뜻하고 실용적
                                길이: 3-5문장
                                """,
                totalStressIndex, stressLevel, topStressors, sleepQuality, sleepLevel);
    }

    /** 일반 메시지 응답 생성 (자유 대화) */
    public static String generateGeneralResponse(
            String userMessage, ConversationPhase currentPhase) {
        String phaseContext =
                switch (currentPhase) {
                    case EMOTION_COLLECTION -> "사용자가 꿈에서 느낀 감정을 파악하는 단계";
                    case CONTEXT_ANALYSIS -> "사용자의 현실 스트레스 상황을 분석하는 단계";
                    case REPORT_GENERATION -> "심리 리포트를 생성하는 단계";
                    case CONTENT_CREATION -> "웹툰을 생성하는 단계";
                };

        return String.format(
                """
                                당신은 드림툰의 AI 심리 상담사입니다.
                                현재 대화 단계: %s

                                사용자 메시지: "%s"

                                다음 원칙에 따라 응답하세요:
                                1. 사용자의 메시지에 공감하고 적절히 답변
                                2. 현재 단계의 목적을 고려하여 대화를 유도
                                3. 질문이 있다면 명확하게 답변
                                4. 자연스럽게 다음 단계로 진행하도록 안내

                                톤: 친근하고 공감적이며 전문적
                                길이: 2-3문장
                                """,
                phaseContext, userMessage);
    }

    /** 감정 타입을 한글 이름으로 변환 */
    private static String getEmotionKoreanName(EmotionType emotion) {
        return switch (emotion) {
            case JOY -> "기쁨";
            case ANXIETY -> "불안";
            case ANGER -> "분노";
            case SADNESS -> "슬픔";
            case SURPRISE -> "놀람";
            case PEACE -> "평온";
        };
    }
}
