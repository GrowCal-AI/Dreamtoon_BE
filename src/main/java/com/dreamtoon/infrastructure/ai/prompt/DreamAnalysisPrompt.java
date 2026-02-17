package com.dreamtoon.infrastructure.ai.prompt;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;

/** GPT-4o 꿈 분석 및 DALL-E 4컷 만화 프롬프트 (Blueprint v2.0) */
public final class DreamAnalysisPrompt {

    private DreamAnalysisPrompt() {}

    /**
     * 꿈 분석용 GPT 프롬프트 생성 (JSON 응답: title, analysis, emotionScores 6종, insight)
     *
     * @param dreamContent 꿈 내용
     * @param primaryEmotion 선택한 주요 감정
     * @param detailedDescription 상세 설명
     * @param realLifeContext 현실 고민 (nullable)
     */
    public static String createDreamAnalysisPrompt(
            String dreamContent,
            EmotionType primaryEmotion,
            String detailedDescription,
            String realLifeContext) {
        String emotionDesc = primaryEmotion != null ? primaryEmotion.getDescription() : "";
        String context =
                realLifeContext != null && !realLifeContext.isBlank() ? realLifeContext : "(없음)";

        return String.format(
                """
                당신은 전문 심리상담사입니다. 사용자의 꿈을 분석하여 심리 상태를 파악하고 조언을 제공하세요.

                [입력 데이터]
                - 꿈 내용: %s
                - 주요 감정: %s
                - 상세 설명: %s
                - 현실 고민: %s

                [출력 형식] 반드시 아래 JSON만 출력하세요. 다른 텍스트는 포함하지 마세요.
                {
                  "title": "꿈 제목 (10자 이내)",
                  "analysis": "꿈 해석 (200자 이내)",
                  "emotionScores": {
                    "기쁨": 0,
                    "불안": 0,
                    "분노": 0,
                    "슬픔": 0,
                    "불편": 0,
                    "평온": 0
                  },
                  "insight": "AI 코칭 메시지 (100자 이내)"
                }
                """,
                dreamContent,
                emotionDesc,
                detailedDescription != null ? detailedDescription : "",
                context);
    }

    /**
     * 4컷 만화 한 컷용 DALL-E 프롬프트 생성
     *
     * @param dreamContent 꿈 내용 (전체 줄거리)
     * @param genre 선택 장르
     * @param panelNumber 현재 컷 번호 (1~4)
     * @param totalPanels 전체 컷 수 (4)
     */
    public static String createWebtoonPanelPrompt(
            String dreamContent, Genre genre, int panelNumber, int totalPanels) {
        String style = genre != null ? genre.getPromptTemplate() : "webtoon style, clean lines";
        String sceneHint =
                String.format(
                        "Panel %d of %d: a single webtoon panel that captures one key moment from"
                                + " this story.",
                        panelNumber, totalPanels);

        return String.format(
                """
                Webtoon style illustration, %s. %s Story summary: %s. Korean manhwa art style, clean lines, professional digital art. Single panel, no text or speech bubbles. Vertical composition suitable for webtoon.
                """,
                style, sceneHint, dreamContent != null ? dreamContent : "a dream scene");
    }
}
