package com.dreamtoon.infrastructure.ai.prompt;

import com.dreamtoon.domain.dream.entity.StylePreset;

/**
 * GPT-4o를 위한 꿈 분석 프롬프트 템플릿
 */
public class DreamAnalysisPrompt {

    /**
     * 꿈을 분석하여 웹툰 장면으로 분할하는 프롬프트
     *
     * @param dreamContent 사용자가 입력한 꿈 내용
     * @param style 웹툰 스타일
     * @return GPT-4o에 전달할 프롬프트
     */
    public static String createSceneAnalysisPrompt(String dreamContent, StylePreset style) {
        return String.format("""
            당신은 전문 웹툰 작가이자 심리 분석가입니다. 사용자의 꿈을 분석하여 웹툰 형식으로 재구성해주세요.

            **꿈 내용:**
            %s

            **웹툰 스타일:** %s

            **요구사항:**
            1. 꿈을 3-6개의 의미 있는 장면으로 분할하세요
            2. 각 장면은 다음 정보를 포함해야 합니다:
               - sceneNumber: 장면 번호 (1부터 시작)
               - description: 시각적 장면 묘사 (50-100자, 웹툰 컷으로 표현 가능한 내용)
               - characters: 등장인물 목록 (배열)
               - emotion: 주요 감정 (JOY, ANXIETY, ANGER, SADNESS, SURPRISE, PEACE 중 하나)
               - backgroundKeywords: 배경 키워드 (예: "숲", "밤", "비" 등, 3-5개)
               - narration: 나레이션 (선택, 30자 이내)
               - dialogue: 대화 배열 (선택, 형식: [{"character": "이름", "text": "대사"}])

            3. 전체 꿈 분석 (analysis):
               - emotions: 6가지 감정별 점수 (JOY, ANXIETY, ANGER, SADNESS, SURPRISE, PEACE, 각 0-100)
               - tensionLevel: 긴장도 (0-100)
               - controlLevel: 통제감/루시드 드림 정도 (0-100)
               - isNightmare: 악몽 여부 (true/false)
               - repeatingSymbols: 반복되는 상징 (배열, 3-5개)
               - relationshipPatterns: 관계 패턴 (배열, 2-3개)
               - hasResolution: 해결 구조 여부 (true/false)
               - aiInsight: AI 인사이트 (50-100자, 심리적 해석)

            **응답 형식 (JSON):**
            {
              "scenes": [
                {
                  "sceneNumber": 1,
                  "description": "...",
                  "characters": ["..."],
                  "emotion": "JOY",
                  "backgroundKeywords": ["..."],
                  "narration": "...",
                  "dialogue": [{"character": "...", "text": "..."}]
                }
              ],
              "analysis": {
                "emotions": {"JOY": 30, "ANXIETY": 10, "ANGER": 5, "SADNESS": 15, "SURPRISE": 20, "PEACE": 20},
                "tensionLevel": 35,
                "controlLevel": 60,
                "isNightmare": false,
                "repeatingSymbols": ["..."],
                "relationshipPatterns": ["..."],
                "hasResolution": true,
                "aiInsight": "..."
              }
            }

            **중요:** 반드시 유효한 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            """, dreamContent, style.getDescription());
    }

    /**
     * DALL-E 3를 위한 이미지 생성 프롬프트
     *
     * @param sceneDescription 장면 설명
     * @param backgroundKeywords 배경 키워드
     * @param style 웹툰 스타일
     * @return DALL-E 3에 전달할 프롬프트
     */
    public static String createImagePrompt(String sceneDescription, String[] backgroundKeywords, StylePreset style) {
        String keywordsStr = String.join(", ", backgroundKeywords);

        return String.format("""
            Create a webtoon panel in %s style.

            Scene: %s
            Background elements: %s

            Art style requirements:
            - %s
            - Vertical composition (portrait orientation, 1024x1792)
            - Clean linework, vibrant colors
            - Webtoon/manhwa aesthetic
            - Professional digital illustration
            - No text or speech bubbles

            Make it visually engaging and emotionally impactful.
            """,
            style.getDescription(),
            sceneDescription,
            keywordsStr,
            style.getPromptTemplate()
        );
    }
}
