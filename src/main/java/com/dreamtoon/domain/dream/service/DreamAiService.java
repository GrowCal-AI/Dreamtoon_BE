package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.analysis.entity.Analysis;
import com.dreamtoon.domain.dream.dto.DreamAnalysisResult;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.StylePreset;
import com.dreamtoon.domain.scene.entity.Scene;
import com.dreamtoon.infrastructure.ai.OpenAiClient;
import com.dreamtoon.infrastructure.ai.prompt.DreamAnalysisPrompt;
import com.dreamtoon.infrastructure.storage.S3StorageService;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.global.error.EntityNotFoundException;
import com.dreamtoon.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 기반 꿈 분석 및 웹툰 생성 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DreamAiService {

    private final OpenAiClient openAiClient;
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper;
    private final DreamRepository dreamRepository;

    /**
     * 비동기로 꿈 분석 시작 (백그라운드 처리)
     *
     * @param dreamId 분석할 꿈 ID
     */
    @Async("dreamProcessingExecutor")
    @Transactional
    public void analyzeDreamAsync(Long dreamId) {
        Dream dream = dreamRepository.findById(dreamId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));

        try {
            log.info("[ASYNC] Starting dream processing for dream ID: {}", dreamId);

            // 상태를 PROCESSING으로 변경
            dream.startProcessing();
            dreamRepository.save(dream);

            // AI 분석 실행 (기존 로직)
            analyzeDream(dream);

            // 상태를 COMPLETED로 변경
            dream.completeProcessing();
            dreamRepository.save(dream);

            log.info("[ASYNC] Dream processing completed successfully for dream ID: {}", dreamId);

        } catch (Exception e) {
            log.error("[ASYNC] Dream processing failed for dream ID: {}", dreamId, e);

            // 상태를 FAILED로 변경
            dream.failProcessing(e.getMessage());
            dreamRepository.save(dream);

            // Fallback 데이터 생성
            createFallbackData(dream);
            dream.completeProcessing(); // Fallback 후에는 COMPLETED로 처리
            dreamRepository.save(dream);

            log.info("[ASYNC] Dream processing completed with fallback data for dream ID: {}", dreamId);
        }
    }

    /**
     * 꿈을 분석하여 장면과 분석 데이터 생성
     *
     * @param dream 분석할 꿈
     */
    public void analyzeDream(Dream dream) {
        try {
            log.info("Starting AI dream analysis for dream ID: {}", dream.getId());

            // 1. GPT-4o로 꿈 분석
            DreamAnalysisResult result = analyzeDreamWithGpt(dream.getRawContent(), dream.getStylePreset());

            // 2. 장면 생성 및 이미지 URL 설정 (임시 URL)
            List<Scene> scenes = createScenesFromAnalysis(result.getScenes(), dream);

            // 3. 분석 데이터 생성
            Analysis analysis = createAnalysisFromResult(result.getAnalysis(), dream);

            // 4. Dream에 연결
            scenes.forEach(dream::addScene);
            dream.setAnalysis(analysis);

            log.info("AI dream analysis completed for dream ID: {}, {} scenes created",
                    dream.getId(), scenes.size());

        } catch (Exception e) {
            log.error("Error during AI dream analysis for dream ID: {}", dream.getId(), e);
            // 실패 시 mock 데이터로 폴백
            createFallbackData(dream);
        }
    }

    /**
     * GPT-4o로 꿈 분석
     */
    private DreamAnalysisResult analyzeDreamWithGpt(String content, StylePreset style) {
        String prompt = DreamAnalysisPrompt.createSceneAnalysisPrompt(content, style);
        String gptResponse = openAiClient.analyzeWithGpt(prompt);

        try {
            // JSON 응답 파싱
            return objectMapper.readValue(gptResponse, DreamAnalysisResult.class);
        } catch (Exception e) {
            log.error("Failed to parse GPT response as JSON", e);
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    /**
     * 분석 결과로부터 Scene 엔티티 생성
     */
    private List<Scene> createScenesFromAnalysis(List<DreamAnalysisResult.SceneDto> sceneDtos, Dream dream) {
        List<Scene> scenes = new ArrayList<>();

        for (DreamAnalysisResult.SceneDto sceneDto : sceneDtos) {
            // DALL-E 3로 이미지 생성
            String imageUrl = generateSceneImage(sceneDto, dream.getStylePreset());

            Scene scene = Scene.builder()
                    .dream(dream)
                    .cutOrder(sceneDto.getSceneNumber())
                    .description(sceneDto.getDescription())
                    .characters(sceneDto.getCharacters())
                    .emotion(sceneDto.getEmotion())
                    .backgroundKeywords(sceneDto.getBackgroundKeywords())
                    .imageUrl(imageUrl)
                    .narration(sceneDto.getNarration())
                    .dialogue(sceneDto.getDialogue())
                    .build();

            scenes.add(scene);
        }

        return scenes;
    }

    /**
     * DALL-E 3로 장면 이미지 생성 및 S3에 업로드
     */
    private String generateSceneImage(DreamAnalysisResult.SceneDto sceneDto, StylePreset style) {
        try {
            String[] keywords = sceneDto.getBackgroundKeywords() != null
                    ? sceneDto.getBackgroundKeywords().toArray(new String[0])
                    : new String[] {};

            String prompt = DreamAnalysisPrompt.createImagePrompt(
                    sceneDto.getDescription(),
                    keywords,
                    style);

            // 1. DALL-E로 이미지 생성 (임시 URL, 60분 유효)
            String tempDalleUrl = openAiClient.generateImage(prompt);
            log.info("Generated DALL-E image for scene {}: {}", sceneDto.getSceneNumber(), tempDalleUrl);

            // 2. S3에 영구 저장
            String permanentS3Url = s3StorageService.uploadImageFromUrl(tempDalleUrl, "scenes");
            log.info("Uploaded scene {} image to S3: {}", sceneDto.getSceneNumber(), permanentS3Url);

            return permanentS3Url;

        } catch (Exception e) {
            log.error("Failed to generate image for scene {}", sceneDto.getSceneNumber(), e);
            // 실패 시 플레이스홀더 이미지
            return "https://via.placeholder.com/1024x1792.png?text=Scene+" + sceneDto.getSceneNumber();
        }
    }

    /**
     * 분석 결과로부터 Analysis 엔티티 생성
     */
    private Analysis createAnalysisFromResult(DreamAnalysisResult.AnalysisDto analysisDto, Dream dream) {
        // emotions의 Double을 그대로 사용 (0-100 범위)
        Map<com.dreamtoon.domain.dream.entity.EmotionType, Double> emotions = analysisDto.getEmotions();

        // healthScore 계산: 평온함과 기쁨은 높을수록 좋고, 불안/분노/슬픔은 낮을수록 좋음
        int healthScore = calculateHealthScore(emotions, analysisDto.getIsNightmare());

        return Analysis.builder()
                .dream(dream)
                .healthScore(healthScore)
                .emotions(emotions)
                .tensionLevel(analysisDto.getTensionLevel())
                .controlLevel(analysisDto.getControlLevel())
                .isNightmare(analysisDto.getIsNightmare())
                .repeatingSymbols(analysisDto.getRepeatingSymbols())
                .relationshipPatterns(analysisDto.getRelationshipPatterns())
                .hasResolution(analysisDto.getHasResolution())
                .aiInsight(analysisDto.getAiInsight())
                .build();
    }

    /**
     * 건강 점수 계산
     */
    private int calculateHealthScore(Map<com.dreamtoon.domain.dream.entity.EmotionType, Double> emotions,
            Boolean isNightmare) {
        double peace = emotions.getOrDefault(com.dreamtoon.domain.dream.entity.EmotionType.PEACE, 0.0);
        double joy = emotions.getOrDefault(com.dreamtoon.domain.dream.entity.EmotionType.JOY, 0.0);
        double anxiety = emotions.getOrDefault(com.dreamtoon.domain.dream.entity.EmotionType.ANXIETY, 0.0);
        double anger = emotions.getOrDefault(com.dreamtoon.domain.dream.entity.EmotionType.ANGER, 0.0);
        double sadness = emotions.getOrDefault(com.dreamtoon.domain.dream.entity.EmotionType.SADNESS, 0.0);

        // 기본 점수: 긍정 감정은 더하고, 부정 감정은 빼기
        double score = (peace + joy) - (anxiety + anger + sadness);

        // 악몽인 경우 추가 감점
        if (Boolean.TRUE.equals(isNightmare)) {
            score -= 20;
        }

        // 0-100 범위로 정규화
        int normalized = (int) Math.max(0, Math.min(100, 50 + score / 2));

        return normalized;
    }

    /**
     * AI 처리 실패 시 폴백 데이터 생성
     */
    private void createFallbackData(Dream dream) {
        log.warn("Using fallback mock data for dream ID: {}", dream.getId());

        // 간단한 mock 장면 1개 생성
        Scene scene = Scene.builder()
                .dream(dream)
                .cutOrder(1)
                .description("AI 분석이 실패하여 기본 장면으로 대체되었습니다.")
                .imageUrl("https://via.placeholder.com/1024x1792.png?text=AI+Processing+Failed")
                .build();

        dream.addScene(scene);

        // 기본 분석 데이터
        Map<com.dreamtoon.domain.dream.entity.EmotionType, Double> emotions = new HashMap<>();
        emotions.put(com.dreamtoon.domain.dream.entity.EmotionType.PEACE, 50.0);

        Analysis analysis = Analysis.builder()
                .dream(dream)
                .healthScore(50)
                .emotions(emotions)
                .tensionLevel(50)
                .controlLevel(50)
                .isNightmare(false)
                .aiInsight("AI 분석을 진행할 수 없었습니다. 잠시 후 다시 시도해주세요.")
                .build();

        dream.setAnalysis(analysis);
    }
}
