package com.dreamtoon.infrastructure.ai.prompt;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;
import java.util.List;

/** GPT-4o 꿈 분석 및 DALL-E 4컷 만화 프롬프트 (Blueprint v3.0) */
public final class DreamAnalysisPrompt {

    private DreamAnalysisPrompt() {}

    /**
     * 꿈 분석용 GPT 프롬프트 생성 (JSON 응답: title, analysis, emotionScores 6종, insight)
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
                각 감정 점수는 0~100 사이 정수로, 꿈에서 느낀 감정의 강도를 나타냅니다.
                최소 2개 이상의 감정에 10 이상의 점수를 부여하세요.
                {
                  "title": "꿈 제목 (10자 이내)",
                  "analysis": "꿈 해석 (200자 이내)",
                  "emotionScores": {
                    "기쁨": 0,
                    "불안": 0,
                    "분노": 0,
                    "슬픔": 0,
                    "놀람": 0,
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
     * 4컷 만화 스토리보드를 GPT로 생성하기 위한 프롬프트.
     * 꿈 내용을 기승전결 4컷으로 나누어 각 컷의 시각적 장면 묘사를 영어로 생성.
     * (DALL-E 3는 영어 프롬프트에서 더 정확한 이미지를 생성)
     *
     * @param dreamContent 꿈 내용
     * @param genre 선택 장르 (스타일 힌트용)
     * @return GPT 프롬프트 (JSON 배열 응답 기대)
     */
    public static String createStoryboardPrompt(String dreamContent, Genre genre) {
        String style = genre != null ? genre.getDescription() : "일반 웹툰";

        return String.format(
                """
                당신은 한국 웹툰 스토리보드 전문 작가이자 DALL-E 이미지 프롬프트 전문가입니다.
                아래 꿈 내용을 읽고, 기승전결 구조의 4컷 만화 스토리보드를 작성하세요.

                [꿈 내용] %s
                [장르/스타일] %s

                ## 핵심 규칙
                1. 꿈의 핵심 주제와 핵심 사건을 정확히 파악하고, 4컷 모두 그 주제를 직접적으로 표현해야 합니다.
                2. 4컷은 반드시 하나의 연결된 이야기여야 합니다. 각 컷이 따로 놀면 안 됩니다.
                3. 주인공의 외모를 매우 구체적으로 정의하세요: 성별, 나이대, 머리 색상과 길이, 옷차림 (예: "a young Korean man in his 20s with short black hair wearing a navy suit").
                   이 외모 묘사를 모든 컷의 장면 묘사에 반복해서 포함하세요.
                4. 각 장면 묘사는 DALL-E가 한 장의 그림으로 그릴 수 있도록 구체적인 시각 요소만 포함하세요.
                5. 장면 묘사는 **영어로** 작성하세요 (DALL-E가 영어 프롬프트를 더 정확하게 이해합니다).
                6. 절대 텍스트, 글자, 말풍선, 제목, 로고를 포함하지 마세요. 순수하게 시각적 묘사만 하세요.

                ## 4컷 구조
                - 1컷(기): 꿈의 시작. 배경과 주인공 소개. 분위기 설정.
                - 2컷(승): 이야기 전개. 꿈의 핵심 사건이 시작됨.
                - 3컷(전): 클라이맥스. 감정이 가장 고조되는 순간.
                - 4컷(결): 결말. 여운이 남는 마무리.

                [출력 형식] 반드시 아래 JSON 배열만 출력하세요. 다른 텍스트는 절대 포함하지 마세요.
                각 원소는 DALL-E용 영어 장면 묘사입니다.
                [
                  "Panel 1: ...",
                  "Panel 2: ...",
                  "Panel 3: ...",
                  "Panel 4: ..."
                ]
                """,
                dreamContent != null ? dreamContent : "알 수 없는 꿈",
                style);
    }

    /**
     * GPT가 생성한 개별 장면 묘사를 DALL-E 이미지 생성 프롬프트로 변환.
     * 전체 스토리 맥락과 전후 컷 정보를 포함하여 일관성 향상.
     *
     * @param sceneDescription GPT가 생성한 현재 컷 장면 묘사 (영어)
     * @param allScenes 전체 4컷 장면 묘사 리스트
     * @param genre 선택 장르
     * @param panelNumber 현재 컷 번호 (1~4)
     * @param totalPanels 전체 컷 수 (4)
     * @param dreamSummary 원본 꿈 내용 요약
     */
    public static String createWebtoonPanelPrompt(
            String sceneDescription,
            List<String> allScenes,
            Genre genre,
            int panelNumber,
            int totalPanels,
            String dreamSummary) {
        String style = genre != null ? genre.getPromptTemplate() : "webtoon style, clean lines";

        // 전후 컷 맥락 구성
        StringBuilder storyContext = new StringBuilder();
        storyContext.append("Full story flow: ");
        for (int i = 0; i < allScenes.size(); i++) {
            if (i == panelNumber - 1) {
                storyContext.append(String.format("[CURRENT Panel %d] ", i + 1));
            } else {
                storyContext.append(String.format("Panel %d: ", i + 1));
            }
            // 다른 컷은 요약만
            String desc = allScenes.get(i);
            if (desc.length() > 80 && i != panelNumber - 1) {
                desc = desc.substring(0, 80) + "...";
            }
            storyContext.append(desc);
            if (i < allScenes.size() - 1) storyContext.append(" → ");
        }

        return String.format(
                """
                A single vertical Korean webtoon panel illustration (panel %d of %d).
                Dream theme: %s

                CURRENT SCENE TO DRAW: %s

                Story context for visual consistency: %s

                Art style: %s.
                Korean manhwa style, professional digital coloring, cinematic composition.
                Portrait orientation (tall vertical format).
                Expressive characters with clear emotions, detailed backgrounds matching the scene.
                The same character must look IDENTICAL across all panels (same hair, clothes, face).
                DO NOT include any text, speech bubbles, letters, words, titles, logos, or watermarks.
                The image must be purely visual with NO written text of any kind.
                """,
                panelNumber, totalPanels, dreamSummary, sceneDescription, storyContext.toString(), style);
    }
}
