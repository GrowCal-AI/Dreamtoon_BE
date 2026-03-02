package com.dreamtoon.infrastructure.ai.prompt;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;
import java.util.List;

/** GPT-4o 꿈 분석 및 DALL-E 4컷 만화 프롬프트 (Blueprint v3.0) */
public final class DreamAnalysisPrompt {

    private DreamAnalysisPrompt() {}

    /** 꿈 분석용 GPT 프롬프트 생성 (JSON 응답: title, analysis, emotionScores 6종, insight) */
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
                  "analysis": "꿈 해석 (250자 이내)",
                  "emotionScores": {
                    "기쁨": 0,
                    "불안": 0,
                    "분노": 0,
                    "슬픔": 0,
                    "놀라움": 0,
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
     * 4컷 만화 스토리보드를 GPT로 생성하기 위한 프롬프트. 꿈 내용을 기승전결 4컷으로 나누어 각 컷의 시각적 장면 묘사를 영어로 생성. Character DNA를 별도
     * 필드로 분리하여 모든 패널에 일관된 캐릭터 적용.
     *
     * @param dreamContent 꿈 내용
     * @param genre 선택 장르 (스타일 힌트용)
     * @return GPT 프롬프트 (JSON 객체 응답 기대: { characterDNA, scenes[] })
     */
    public static String createStoryboardPrompt(String dreamContent, Genre genre) {
        String style = genre != null ? genre.getDescription() : "일반 웹툰";

        return String.format(
                """
                You are a professional Korean webtoon storyboard artist and AI image prompt expert.
                Read the dream below and create a 4-panel storyboard with the 기승전결 structure.

                [Dream Content] %s
                [Genre/Style] %s

                ## Critical Rules
                1. Identify the CORE theme and KEY events of the dream. ALL 4 panels must directly depict this theme.
                2. The 4 panels must tell ONE connected story. Each panel flows into the next.
                3. Define a CHARACTER DNA: a detailed, fixed appearance description for the protagonist.
                   Include: gender, age range, ethnicity, hair color/style/length, eye features, clothing, and one unique visual trait.
                   Example: "A Korean woman in her late 20s, shoulder-length straight black hair with side bangs, wearing a cream knit sweater and dark blue jeans, silver bracelet on left wrist"
                4. Each scene description must be a VISUAL-ONLY prompt suitable for AI image generation. Describe what the camera would see.
                5. Write ALL scene descriptions in English.
                6. NEVER mention text, speech bubbles, letters, titles, logos, or watermarks in scene descriptions.
                7. Start each scene description with the full Character DNA so every panel depicts the same person.

                ## 4-Panel Structure
                - Panel 1 (기/Setup): Dream begins. Introduce protagonist and setting. Establish mood.
                - Panel 2 (승/Development): Story progresses. The core dream event begins.
                - Panel 3 (전/Climax): Peak emotion. The most intense or dramatic moment.
                - Panel 4 (결/Resolution): Ending. Leave a lasting impression or emotional afterglow.

                [Output Format] Return ONLY this JSON object. No other text.
                {
                  "characterDNA": "Full character appearance description in English (one sentence, very specific)",
                  "scenes": [
                    "Panel 1: [Character DNA]. [Scene description with setting, action, emotion, lighting]",
                    "Panel 2: [Character DNA]. [Scene description with setting, action, emotion, lighting]",
                    "Panel 3: [Character DNA]. [Scene description with setting, action, emotion, lighting]",
                    "Panel 4: [Character DNA]. [Scene description with setting, action, emotion, lighting]"
                  ]
                }
                """,
                dreamContent != null ? dreamContent : "Unknown dream", style);
    }

    /**
     * GPT가 생성한 개별 장면 묘사를 이미지 생성 프롬프트로 변환. Character DNA를 명시적으로 포함하여 캐릭터 일관성 향상. DALL-E 3 및 FLUX 모두
     * 호환.
     *
     * @param sceneDescription GPT가 생성한 현재 컷 장면 묘사 (영어)
     * @param characterDNA 주인공 외모 묘사 (영어, 모든 패널 동일)
     * @param genre 선택 장르
     * @param panelNumber 현재 컷 번호 (1~4)
     * @param totalPanels 전체 컷 수 (4)
     */
    public static String createWebtoonPanelPrompt(
            String sceneDescription,
            String characterDNA,
            Genre genre,
            int panelNumber,
            int totalPanels) {
        String style = genre != null ? genre.getPromptTemplate() : "webtoon style, clean lines";

        // 프리미엄 장르는 고유 스타일이 강하므로 "Korean manhwa" 수식을 붙이지 않음
        // 스탠다드 장르(CUSTOM 등)는 Korean manhwa 베이스 유지
        boolean isPremium = genre != null && genre.isPremium();
        String artStyleInstruction =
                isPremium
                        ? style + ", high quality digital illustration, cinematic composition"
                        : style
                                + ", Korean manhwa, professional digital coloring, cinematic"
                                + " composition";

        return String.format(
                """
                %s

                Character: %s

                Art style: %s
                Portrait orientation. Expressive emotions, detailed background.
                NO text, NO speech bubbles, NO letters, NO titles, NO logos, NO watermarks. Purely visual.
                Family-friendly illustration. Fantastical and dreamlike mood. No realistic violence, \
                no gore, no disturbing imagery. Scary elements depicted as whimsical and cartoonish, \
                not threatening.
                """,
                sceneDescription, characterDNA, artStyleInstruction);
    }

    /**
     * 4컷 장면 묘사 전체를 단일 2x2 그리드 만화 이미지로 생성하기 위한 프롬프트.
     *
     * @param scenes 4개 장면 묘사 리스트 (영어)
     * @param characterDNA 주인공 외모 묘사 (영어)
     * @param genre 선택 장르
     */
    public static String createComicStripPrompt(
            List<String> scenes, String characterDNA, Genre genre) {
        String style = genre != null ? genre.getPromptTemplate() : "webtoon style, clean lines";
        boolean isPremium = genre != null && genre.isPremium();
        String artStyle =
                isPremium
                        ? style + ", high quality digital illustration, cinematic composition"
                        : style
                                + ", Korean manhwa, professional digital coloring, cinematic"
                                + " composition";

        return String.format(
                """
                A single square image divided into exactly 4 equal comic panels in a 2x2 grid layout, \
                with clear black borders separating each panel.

                Panel 1 (top-left): %s

                Panel 2 (top-right): %s

                Panel 3 (bottom-left): %s

                Panel 4 (bottom-right): %s

                Character appearing throughout all panels: %s

                Art style for all panels: %s
                Expressive emotions, detailed backgrounds. Consistent character appearance across all panels.
                NO text, NO speech bubbles, NO letters, NO titles, NO logos, NO watermarks. Purely visual.
                Family-friendly illustration. Fantastical and dreamlike mood. No realistic violence, \
                no gore, no disturbing imagery. Scary elements depicted as whimsical and cartoonish, \
                not threatening.
                """,
                scenes.get(0), scenes.get(1), scenes.get(2), scenes.get(3), characterDNA, artStyle);
    }
}
