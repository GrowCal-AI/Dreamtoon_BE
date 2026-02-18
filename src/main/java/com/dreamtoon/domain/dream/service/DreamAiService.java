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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AI 기반 꿈 분석 및 4컷 웹툰 생성 서비스 (Blueprint v3.0) */
@Slf4j
@Service
@RequiredArgsConstructor
public class DreamAiService {

    private static final int WEBTOON_PANEL_COUNT = 4;

    /** 4컷 이미지를 확실히 동시 생성하기 위한 전용 스레드 풀 (4개 고정) */
    private final ExecutorService imageGenerationPool = Executors.newFixedThreadPool(4);

    private final OpenAiClient openAiClient;
    private final ImageGenerationProvider imageGenerationProvider;
    private final GcsStorageService gcsStorageService;
    private final ObjectMapper objectMapper;
    private final DreamRepository dreamRepository;

    /** 비동기 꿈 분석 (GPT-4o → Dream 필드 저장, ANALYSIS_COMPLETED) */
    @Async("dreamProcessingExecutor")
    @Transactional
    public void analyzeDreamAsync(Long dreamId) {
        Dream dream =
                dreamRepository
                        .findById(dreamId)
                        .orElseThrow(
                                () ->
                                        new com.dreamtoon.global.error.EntityNotFoundException(
                                                com.dreamtoon.global.error.ErrorCode
                                                        .DREAM_NOT_FOUND));

        try {
            log.info("[ASYNC] Starting dream analysis for dream ID: {}", dreamId);
            dream.startAnalyzing();
            dreamRepository.save(dream);

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

            dream.completeAnalysis(
                    result.getTitle(),
                    result.getAnalysis(),
                    result.getEmotionScores() != null
                            ? result.getEmotionScores()
                            : new java.util.HashMap<>(),
                    result.getInsight());
            dreamRepository.save(dream);

            log.info("[ASYNC] Dream analysis completed for dream ID: {}", dreamId);
        } catch (Exception e) {
            log.error("[ASYNC] Dream analysis failed for dream ID: {}", dreamId, e);
            dream.failProcessing(e.getMessage());
            dreamRepository.save(dream);
        }
    }

    /**
     * 비동기 4컷 웹툰 생성 (2단계: GPT 스토리보드 → DALL-E 4회 병렬 → GCS 업로드) 1단계: GPT가 꿈 내용을 기승전결 4컷 장면으로 분할 (스토리
     * 연결성 보장) 2단계: 각 장면 묘사를 DALL-E에 병렬로 전달하여 이미지 생성 (실패 시 1회 재시도)
     */
    @Async("dreamProcessingExecutor")
    @Transactional
    public void generateWebtoonAsync(Long dreamId) {
        Dream dream =
                dreamRepository
                        .findById(dreamId)
                        .orElseThrow(
                                () ->
                                        new com.dreamtoon.global.error.EntityNotFoundException(
                                                com.dreamtoon.global.error.ErrorCode
                                                        .DREAM_NOT_FOUND));

        try {
            log.info("[ASYNC] Starting webtoon generation for dream ID: {}", dreamId);
            dream.startGenerating();
            dreamRepository.save(dream);

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

            // ── 2단계: 각 장면을 이미지 생성 프로바이더로 병렬 생성 (재시도 포함) ──
            final List<String> finalScenes = List.copyOf(sceneDescriptions);
            final String finalCharDNA = characterDNA;
            log.info("[IMAGE] Using provider: {}", imageGenerationProvider.getProviderName());

            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (int panel = 1; panel <= WEBTOON_PANEL_COUNT; panel++) {
                final int panelNum = panel;
                final String sceneDesc = finalScenes.get(panel - 1);
                CompletableFuture<String> future =
                        CompletableFuture.supplyAsync(
                                () ->
                                        generateSinglePanel(
                                                dreamId, panelNum, sceneDesc, finalCharDNA, dream),
                                imageGenerationPool);
                futures.add(future);
            }

            // 각 패널 결과를 개별적으로 수집 (실패한 것은 null)
            List<String> imageUrls = new ArrayList<>();
            for (int i = 0; i < futures.size(); i++) {
                try {
                    imageUrls.add(futures.get(i).join());
                } catch (Exception e) {
                    log.error(
                            "[PANEL {}] Final failure for dream ID: {}: {}",
                            i + 1,
                            dreamId,
                            e.getMessage());
                    imageUrls.add(null);
                }
            }

            // null이 아닌 성공한 패널만 필터링
            List<String> successUrls = imageUrls.stream().filter(url -> url != null).toList();

            if (successUrls.isEmpty()) {
                throw new RuntimeException("모든 패널 생성 실패");
            }

            // 최소 1개 성공이면 저장 (4개 모두 성공이 이상적)
            dream.completeGeneration(successUrls);
            dreamRepository.save(dream);

            log.info(
                    "[ASYNC] Webtoon generation completed for dream ID: {} ({}/{} panels)",
                    dreamId,
                    successUrls.size(),
                    WEBTOON_PANEL_COUNT);
        } catch (Exception e) {
            log.error("[ASYNC] Webtoon generation failed for dream ID: {}", dreamId, e);
            dream.failProcessing(e.getMessage());
            dreamRepository.save(dream);
        }
    }

    /** 단일 패널 이미지 생성 (재시도 시 프롬프트 완화) */
    private String generateSinglePanel(
            Long dreamId, int panelNum, String sceneDesc, String characterDNA, Dream dream) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String currentScene = sceneDesc;
                // 2차 시도부터는 프롬프트를 더 안전하게 완화 (content policy 회피)
                if (attempt >= 2) {
                    currentScene = softenPromptForRetry(sceneDesc);
                    log.info("[PANEL {}] Retrying with softened prompt", panelNum);
                }

                log.info(
                        "[PANEL {}] Attempt {}/{} via {} on thread: {}",
                        panelNum,
                        attempt,
                        maxRetries,
                        imageGenerationProvider.getProviderName(),
                        Thread.currentThread().getName());
                String prompt =
                        DreamAnalysisPrompt.createWebtoonPanelPrompt(
                                currentScene,
                                characterDNA,
                                dream.getSelectedGenre(),
                                panelNum,
                                WEBTOON_PANEL_COUNT);
                String tempUrl = imageGenerationProvider.generateImage(prompt);
                String gcsPrefix = String.format("webtoon/dream_%d/panel_%d", dreamId, panelNum);
                String gcsUrl = gcsStorageService.uploadImageFromUrl(tempUrl, gcsPrefix);
                log.info("[PANEL {}] Done: {}", panelNum, gcsUrl);
                return gcsUrl;
            } catch (Exception e) {
                log.warn(
                        "[PANEL {}] Attempt {}/{} failed: {}",
                        panelNum,
                        attempt,
                        maxRetries,
                        e.getMessage());
                if (attempt == maxRetries) {
                    throw new RuntimeException(
                            "Panel " + panelNum + " 생성 실패 (재시도 소진): " + e.getMessage(), e);
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
