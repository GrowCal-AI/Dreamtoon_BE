package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.dream.dto.*;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.global.error.BusinessException;
import com.dreamtoon.global.error.EntityNotFoundException;
import com.dreamtoon.global.error.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 비회원 꿈 미리보기 서비스.
 *
 * <p>로그인 없이 1회 꿈 분석 + 웹툰 생성을 체험할 수 있다. guestToken(UUID)을 발급하고, 이후 모든 요청에서 해당 토큰으로 본인 꿈임을 검증한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestDreamService {

    private static final String CREATE_MESSAGE = "꿈 미리보기를 시작합니다. AI가 꿈을 분석하고 있습니다.";
    private static final String WEBTOON_MESSAGE = "4컷 만화 생성을 시작했습니다. 완료될 때까지 잠시 기다려 주세요.";

    private final DreamRepository dreamRepository;
    private final DreamAiService dreamAiService;

    /** 비회원 꿈 생성 + AI 분석 트리거 */
    @Transactional
    public GuestDreamCreateResponse createGuestDream(GuestDreamCreateRequest request) {
        String guestToken = UUID.randomUUID().toString();

        Dream dream =
                Dream.builder()
                        .guestToken(guestToken)
                        .dreamContent(request.getContent())
                        .primaryEmotion(request.getMainEmotion())
                        .selectedGenre(request.getStyle())
                        .build();
        dreamRepository.save(dream);

        dreamAiService.analyzeDreamAsync(dream.getId());

        log.info("Guest dream created, ID: {}", dream.getId());
        return GuestDreamCreateResponse.builder()
                .dreamId(dream.getId())
                .guestToken(guestToken)
                .message(CREATE_MESSAGE)
                .build();
    }

    /** 비회원 분석 결과 조회 (폴링용) */
    public DreamAnalysisResponse getAnalysis(Long dreamId, String guestToken) {
        Dream dream = findGuestDream(dreamId, guestToken);
        ProcessingStatus status = dream.getProcessingStatus();

        if (status != ProcessingStatus.ANALYSIS_COMPLETED && status != ProcessingStatus.COMPLETED) {
            return DreamAnalysisResponse.builder().dreamId(dreamId).status(status).build();
        }

        return DreamAnalysisResponse.builder()
                .dreamId(dreamId)
                .status(status)
                .title(dream.getTitle())
                .aiAnalysis(dream.getAiAnalysis())
                .emotionScores(dream.getEmotionScores())
                .aiInsight(dream.getAiInsight())
                .build();
    }

    /** 비회원 웹툰 생성 트리거 (스타일 재선택 가능) */
    @Transactional
    public WebtoonGenerateResponse generateWebtoon(
            Long dreamId, String guestToken, WebtoonGenerateRequest request) {
        Dream dream = findGuestDream(dreamId, guestToken);

        if (dream.getProcessingStatus() != ProcessingStatus.ANALYSIS_COMPLETED) {
            throw new BusinessException(ErrorCode.DREAM_INVALID_STATE);
        }

        Genre selectedGenre = request.getSelectedGenre();
        if (selectedGenre.isPremium()) {
            throw new BusinessException(ErrorCode.PREMIUM_STYLE_NOT_ALLOWED);
        }

        dream.selectGenre(selectedGenre);
        dreamRepository.save(dream);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        dreamAiService.generateWebtoonAsync(dreamId);
                    }
                });

        return WebtoonGenerateResponse.builder()
                .dreamId(dreamId)
                .status(ProcessingStatus.GENERATING)
                .message(WEBTOON_MESSAGE)
                .build();
    }

    /** 비회원 꿈 상세 조회 */
    public DreamResponse getDream(Long dreamId, String guestToken) {
        Dream dream = findGuestDream(dreamId, guestToken);
        return DreamResponse.from(dream);
    }

    private Dream findGuestDream(Long dreamId, String guestToken) {
        return dreamRepository
                .findByIdAndGuestToken(dreamId, guestToken)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));
    }
}
