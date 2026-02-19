package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.dream.dto.AiAnalysisResult;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.infrastructure.ai.ImageGenerationProvider;
import com.dreamtoon.infrastructure.ai.OpenAiClient;
import com.dreamtoon.infrastructure.ai.prompt.DreamAnalysisPrompt;
import com.dreamtoon.infrastructure.storage.GcsStorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** AI 기반 꿈 분석 및 4컷 웹툰 생성 서비스 (Blueprint v3.0) */
@Slf4j
@Service
@RequiredArgsConstructor
public class DreamAiService {

    private static final int WEBTOON_PANEL_COUNT = 4;

    private final OpenAiClient openAiClient;
    private final ImageGenerationProvider imageGenerationProvider;
    private final GcsStorageService gcsStorageService;
    private final ObjectMapper objectMapper;
    private final DreamRepository dreamRepository;
    private final TransactionTemplate transactionTemplate;

    /** 비동기 꿈 분석 (GPT-4o → Dream 필드 저장, ANALYSIS_COMPLETED) */
    @Async("dreamProcessingExecutor")
    public void analyzeDreamAsync(Long dreamId) {
        // DB 커넥션을 짧게 사용: 로드 + 상태 업데이트만
        Dream dream =
                transactionTemplate.execute(
                        status -> {
                            Dream d =
                                    dreamRepository
                                            .findById(dreamId)
                                            .orElseThrow(
                                                    () ->
                                                            new com.dreamtoon.global.error
                                                                    .EntityNotFoundException(
                                                                    com.dreamtoon.global.error
                                                                            .ErrorCode
                                                                            .DREAM_NOT_FOUND));
                            d.startAnalyzing();
                            return dreamRepository.save(d);
                        });

        try {
            log.info("[ASYNC] Starting dream analysis for dream ID: {}", dreamId);

            String prompt =
                    DreamAnalysisPrompt.createDreamAnalysisPrompt(
                            dream.getDreamContent(),
                            dream.getPrimaryEmotion(),
                            dream.getDetailedDescription(),
                            dream.getRealLifeContext());

            String gptResponse = openAiClient.analyzeWithGpt(prompt);
            // GPT 응답에서 마크다운 코드 블록 제거 (예: ```json\n{...}```)
            gptResponse = gptResponse.replaceAll("```json\\n|```", "");
            AiAnalysisResult result = objectMapper.readValue(gptResponse, AiAnalysisResult.class);

            // DB 커넥션을 짧게 사용: 분석 결과 저장만
            final Dream analyzedDream = dream;
            transactionTemplate.executeWithoutResult(
                    status -> {
                        analyzedDream.completeAnalysis(
                                result.getTitle(),
                                result.getAnalysis(),
                                result.getEmotionScores() != null
                                        ? result.getEmotionScores()
                                        : new java.util.HashMap<>(),
                                result.getInsight());
                        dreamRepository.save(analyzedDream);
                    });

            log.info("[ASYNC] Dream analysis completed for dream ID: {}", dreamId);
        } catch (Exception e) {
            log.error("[ASYNC] Dream analysis failed for dream ID: {}", dreamId, e);
            final Dream failedDream = dream;
            transactionTemplate.executeWithoutResult(
                    status -> {
                        failedDream.failProcessing(e.getMessage());
                        dreamRepository.save(failedDream);
                    });
        }
    }

    /**
     * 비동기 4컷 웹툰 생성 (2단계: GPT 스토리보드 → DALL-E 4회 병렬 → GCS 업로드) 1단계: GPT가 꿈 내용을 기승전결 4컷 장면으로 분할 (스토리
     * 연결성 보장) 2단계: 각 장면 묘사를 DALL-E에 병렬로 전달하여 이미지 생성 (실패 시 1회 재시도)
     */
    @Async("dreamProcessingExecutor")
    public void generateWebtoonAsync(Long dreamId) {
        // DB 커넥션을 짧게 사용: 로드 + 상태 업데이트만
        Dream dream =
                transactionTemplate.execute(
                        status -> {
                            Dream d =
                                    dreamRepository
                                            .findById(dreamId)
                                            .orElseThrow(
                                                    () ->
                                                            new com.dreamtoon.global.error
                                                                    .EntityNotFoundException(
                                                                    com.dreamtoon.global.error
                                                                            .ErrorCode
                                                                            .DREAM_NOT_FOUND));
                            d.startGenerating();
                            return dreamRepository.save(d);
                        });

        try {
            log.info("[ASYNC] Starting webtoon generation for dream ID: {}", dreamId);

            // ── 1단계: GPT로 4컷 스토리보드 생성 (기승전결 연결) ──
            log.info(
                    "[STORYBOARD] Generating 4-panel storyboard via GPT for dream ID: {}", dreamId);
            String storyboardPrompt =
                    DreamAnalysisPrompt.createStoryboardPrompt(
                            dream.getDreamContent(), dream.getSelectedGenre());
            String storyboardResponse = openAiClient.analyzeWithGpt(storyboardPrompt);

            // GPT 응답에서 마크다운 코드 블록 제거
            storyboardResponse = storyboardResponse.replaceAll("```json\\n|```", "").trim();

            // 새 형식: { "characterDNA": "...", "scenes": [...] }
            String characterDNA = "";
            List<String> sceneDescriptions;

            JsonNode storyboardNode = objectMapper.readTree(storyboardResponse);
            if (storyboardNode.has("characterDNA") && storyboardNode.has("scenes")) {
                characterDNA = storyboardNode.get("characterDNA").asText("");
                sceneDescriptions =
                        objectMapper.readValue(
                                storyboardNode.get("scenes").toString(),
                                objectMapper
                                        .getTypeFactory()
                                        .constructCollectionType(List.class, String.class));
                log.info("[STORYBOARD] Character DNA: {}", characterDNA);
            } else {
                // 폴백: 기존 배열 형식 호환
                log.warn("[STORYBOARD] Fallback to array format (no characterDNA)");
                sceneDescriptions =
                        objectMapper.readValue(
                                storyboardResponse,
                                objectMapper
                                        .getTypeFactory()
                                        .constructCollectionType(List.class, String.class));
            }

            if (sceneDescriptions.size() != WEBTOON_PANEL_COUNT) {
                log.warn(
                        "[STORYBOARD] Expected {} scenes but got {}. Padding/trimming.",
                        WEBTOON_PANEL_COUNT,
                        sceneDescriptions.size());
                while (sceneDescriptions.size() < WEBTOON_PANEL_COUNT) {
                    sceneDescriptions.add(sceneDescriptions.get(sceneDescriptions.size() - 1));
                }
                sceneDescriptions = sceneDescriptions.subList(0, WEBTOON_PANEL_COUNT);
            }
            log.info("[STORYBOARD] 4-panel storyboard ready for dream ID: {}", dreamId);

            // ── 2단계: 4컷 만화 단일 이미지 생성 ──
            log.info("[IMAGE] Using provider: {}", imageGenerationProvider.getProviderName());
            String comicUrl = generateComicStrip(dreamId, sceneDescriptions, characterDNA, dream);
            List<String> successUrls = List.of(comicUrl);

            // DB 커넥션을 짧게 사용: 완료 결과 저장만
            final Dream completedDream = dream;
            final List<String> finalUrls = successUrls;
            transactionTemplate.executeWithoutResult(
                    status -> {
                        completedDream.completeGeneration(finalUrls);
                        dreamRepository.save(completedDream);
                    });

            log.info("[ASYNC] Webtoon generation completed for dream ID: {}", dreamId);
        } catch (Exception e) {
            log.error("[ASYNC] Webtoon generation failed for dream ID: {}", dreamId, e);
            final Dream failedDream = dream;
            transactionTemplate.executeWithoutResult(
                    status -> {
                        failedDream.failProcessing(e.getMessage());
                        dreamRepository.save(failedDream);
                    });
        }
    }

    /** 4컷 만화를 단일 이미지로 생성 (재시도 시 프롬프트 완화) */
    private String generateComicStrip(
            Long dreamId, List<String> scenes, String characterDNA, Dream dream) {
        int maxRetries = 2;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                List<String> currentScenes = scenes;
                if (attempt >= 2) {
                    currentScenes = scenes.stream().map(this::softenPromptForRetry).toList();
                    log.info("[COMIC] Retrying with softened scenes");
                }

                log.info(
                        "[COMIC] Attempt {}/{} via {} on thread: {}",
                        attempt,
                        maxRetries,
                        imageGenerationProvider.getProviderName(),
                        Thread.currentThread().getName());

                String prompt =
                        DreamAnalysisPrompt.createComicStripPrompt(
                                currentScenes, characterDNA, dream.getSelectedGenre());
                String tempUrl = imageGenerationProvider.generateImage(prompt);
                String gcsPrefix = String.format("webtoon/dream_%d/comic_strip", dreamId);
                String gcsUrl = gcsStorageService.uploadImageFromUrl(tempUrl, gcsPrefix);
                log.info("[COMIC] Done: {}", gcsUrl);
                return gcsUrl;
            } catch (Exception e) {
                log.warn("[COMIC] Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt == maxRetries) {
                    throw new RuntimeException("Comic strip 생성 실패 (재시도 소진): " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException("Unreachable");
    }

    /** content policy 거부 시 프롬프트를 완화. 위험한 키워드를 제거하고 더 부드러운 표현으로 교체. */
    private String softenPromptForRetry(String originalPrompt) {
        return originalPrompt
                        .replaceAll(
                                "(?i)(blood|gore|violent|weapon|knife|gun|sword|dead|death|kill)",
                                "dramatic tension")
                        .replaceAll(
                                "(?i)(horror|terrifying|scary|nightmare)",
                                "mysterious and dreamlike")
                        .replaceAll("(?i)(naked|nude|exposed)", "ethereal figure")
                        .replaceAll("(?i)(fire|burning|explosion)", "glowing light")
                        .replaceAll("(?i)(crying|tears|scream)", "emotional expression")
                + "\n"
                + "Gentle, dreamy atmosphere. Safe for all audiences. Family-friendly"
                + " illustration.";
    }
}
