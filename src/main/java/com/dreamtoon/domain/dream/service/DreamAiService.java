package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.dream.dto.AiAnalysisResult;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.infrastructure.ai.OpenAiClient;
import com.dreamtoon.infrastructure.ai.prompt.DreamAnalysisPrompt;
import com.dreamtoon.infrastructure.storage.GcsStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AI 기반 꿈 분석 및 4컷 웹툰 생성 서비스 (Blueprint v2.0) */
@Slf4j
@Service
@RequiredArgsConstructor
public class DreamAiService {

    private static final int WEBTOON_PANEL_COUNT = 4;

    private final OpenAiClient openAiClient;
    private final GcsStorageService gcsStorageService;
    private final ObjectMapper objectMapper;
    private final DreamRepository dreamRepository;

    /**
     * 비동기 꿈 분석 (GPT-4o → Dream 필드 저장, ANALYSIS_COMPLETED)
     *
     * @param dreamId 분석할 꿈 ID
     */
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
     * 비동기 4컷 웹툰 생성 (DALL-E 4회 → GCS 업로드 → Dream.webtoonImages, COMPLETED)
     *
     * @param dreamId 생성할 꿈 ID
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

            // 4컷 이미지를 병렬로 동시 생성 (순차 대비 ~4배 빠름)
            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (int panel = 1; panel <= WEBTOON_PANEL_COUNT; panel++) {
                final int panelNum = panel;
                CompletableFuture<String> future =
                        CompletableFuture.supplyAsync(
                                () -> {
                                    String prompt =
                                            DreamAnalysisPrompt.createWebtoonPanelPrompt(
                                                    dream.getDreamContent(),
                                                    dream.getSelectedGenre(),
                                                    panelNum,
                                                    WEBTOON_PANEL_COUNT);
                                    String tempUrl = openAiClient.generateImage(prompt);
                                    return gcsStorageService.uploadImageFromUrl(tempUrl, "webtoon");
                                });
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            List<String> imageUrls = futures.stream().map(CompletableFuture::join).toList();

            dream.completeGeneration(imageUrls);
            dreamRepository.save(dream);

            log.info("[ASYNC] Webtoon generation completed for dream ID: {}", dreamId);
        } catch (Exception e) {
            log.error("[ASYNC] Webtoon generation failed for dream ID: {}", dreamId, e);
            dream.failProcessing(e.getMessage());
            dreamRepository.save(dream);
        }
    }
}
