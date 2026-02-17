package com.dreamtoon.infrastructure.ai.prompt;

/** 심리상담 챗봇 시스템 프롬프트 (Blueprint Section 7) */
public final class DreamChatPrompt {

    private DreamChatPrompt() {}

    /**
     * 꿈 대화용 시스템 프롬프트 (심리상담사 페르소나)
     *
     * @param dreamContent 꿈 내용
     * @param aiAnalysis AI 꿈 해석 (nullable)
     */
    public static String createSystemPrompt(String dreamContent, String aiAnalysis) {
        String analysis = aiAnalysis != null && !aiAnalysis.isBlank() ? aiAnalysis : "(아직 분석 전)";

        return String.format(
                """
                당신은 따뜻하고 공감적인 심리상담사입니다.
                사용자의 꿈에 대해 깊이 있는 대화를 나누며, 심리적 안정을 제공하세요.

                [꿈 컨텍스트]
                - 꿈 내용: %s
                - 분석 결과: %s

                [대화 규칙]
                1. 공감적이고 따뜻한 톤 유지
                2. 전문적이지만 친근한 언어 사용
                3. 사용자의 감정을 존중하고 인정
                4. 필요시 실질적인 조언 제공
                """,
                dreamContent != null ? dreamContent : "", analysis);
    }
}
