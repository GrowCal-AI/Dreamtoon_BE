package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.dream.constants.EmotionMessages;
import com.dreamtoon.domain.dream.dto.*;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.common.dto.response.PageResponse;
import com.dreamtoon.global.error.BusinessException;
import com.dreamtoon.global.error.EntityNotFoundException;
import com.dreamtoon.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 꿈 기록 및 웹툰 생성 서비스 (Blueprint v2.0) */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DreamService {

    private static final String INITIATE_MESSAGE = "꿈 내용을 입력해 주셔서 감사합니다. 이 꿈에서 느낀 감정을 선택해 주세요.";
    private static final String DETAILS_MESSAGE = "상세 설명이 저장되었습니다. AI가 꿈을 분석하고 있습니다.";
    private static final String WEBTOON_MESSAGE = "4컷 만화 생성을 시작했습니다. 완료될 때까지 잠시 기다려 주세요.";

    private final DreamRepository dreamRepository;
    private final UserRepository userRepository;
    private final DreamAiService dreamAiService;
    private final SubscriptionService subscriptionService;

    /** FE 통합 꿈 생성 (감정 → 내용 → 스타일 한번에) */
    @Transactional
    public DreamResponse createDreamFull(Long userId, CreateDreamFullRequest request) {
        // 스탠다드 쿼터 체크 (꿈 생성 = 스탠다드 이미지 소비)
        if (!subscriptionService.canGenerateStandard(userId)) {
            throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Dream dream =
                Dream.builder()
                        .user(user)
                        .dreamContent(request.getContent())
                        .primaryEmotion(request.getMainEmotion())
                        .selectedGenre(request.getStyle())
                        .title(request.getTitle())
                        .build();
        dreamRepository.save(dream);
        subscriptionService.incrementStandardGeneration(userId);

        dreamAiService.analyzeDreamAsync(dream.getId());

        log.info("Dream created (full), ID: {}", dream.getId());
        return DreamResponse.from(dream);
    }

    @Transactional
    public InitiateDreamResponse initiateDream(Long userId, InitiateDreamRequest request) {
        if (!subscriptionService.canGenerateStandard(userId)) {
            throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Dream dream = Dream.builder().user(user).dreamContent(request.getDreamContent()).build();
        dreamRepository.save(dream);
        subscriptionService.incrementStandardGeneration(userId);

        log.info("Dream initiated, ID: {}", dream.getId());
        return InitiateDreamResponse.builder()
                .dreamId(dream.getId())
                .systemMessage(INITIATE_MESSAGE)
                .build();
    }

    @Transactional
    public EmotionSelectResponse selectEmotion(
            Long userId, Long dreamId, EmotionSelectRequest request) {
        Dream dream = findDreamByUser(dreamId, userId);
        if (dream.getProcessingStatus() != ProcessingStatus.PENDING) {
            throw new BusinessException(ErrorCode.DREAM_INVALID_STATE);
        }

        dream.selectEmotion(request.getPrimaryEmotion());
        dreamRepository.save(dream);

        String message = EmotionMessages.getMessage(request.getPrimaryEmotion());
        return EmotionSelectResponse.builder().systemMessage(message).build();
    }

    @Transactional
    public DreamDetailsResponse addDetails(Long userId, Long dreamId, DreamDetailsRequest request) {
        Dream dream = findDreamByUser(dreamId, userId);
        if (dream.getProcessingStatus() != ProcessingStatus.PENDING) {
            throw new BusinessException(ErrorCode.DREAM_INVALID_STATE);
        }

        dream.addDetails(request.getDetailedDescription(), request.getRealLifeContext());
        dreamRepository.save(dream);

        dreamAiService.analyzeDreamAsync(dreamId);

        return DreamDetailsResponse.builder()
                .dreamId(dreamId)
                .status(ProcessingStatus.ANALYZING)
                .message(DETAILS_MESSAGE)
                .build();
    }

    public DreamAnalysisResponse getAnalysis(Long userId, Long dreamId) {
        Dream dream = findDreamByUser(dreamId, userId);
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

    @Transactional
    public WebtoonGenerateResponse generateWebtoon(
            Long userId, Long dreamId, WebtoonGenerateRequest request) {
        Dream dream = findDreamByUser(dreamId, userId);
        if (dream.getProcessingStatus() != ProcessingStatus.ANALYSIS_COMPLETED) {
            throw new BusinessException(ErrorCode.DREAM_INVALID_STATE);
        }

        com.dreamtoon.domain.dream.entity.Genre selectedGenre = request.getSelectedGenre();

        // 프리미엄 필터 쿼터 체크
        if (selectedGenre.isPremium()) {
            if (!subscriptionService.canGeneratePremium(userId)) {
                throw new BusinessException(ErrorCode.PREMIUM_STYLE_NOT_ALLOWED);
            }
            subscriptionService.incrementPremiumGeneration(userId);
        } else {
            // 스탠다드 필터는 스탠다드 쿼터 소비
            if (!subscriptionService.canGenerateStandard(userId)) {
                throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
            }
            subscriptionService.incrementStandardGeneration(userId);
        }

        dream.selectGenre(selectedGenre);
        dreamRepository.save(dream);

        dreamAiService.generateWebtoonAsync(dreamId);

        return WebtoonGenerateResponse.builder()
                .dreamId(dreamId)
                .status(ProcessingStatus.GENERATING)
                .message(WEBTOON_MESSAGE)
                .build();
    }

    public DreamResponse getDream(Long userId, Long dreamId) {
        Dream dream = findDreamByUser(dreamId, userId);
        return DreamResponse.from(dream);
    }

    public PageResponse<DreamResponse> getUserDreams(Long userId, Pageable pageable) {
        Page<Dream> dreams = dreamRepository.findByUserId(userId, pageable);
        return PageResponse.of(dreams.map(DreamResponse::from));
    }

    @Transactional
    public void deleteDream(Long userId, Long dreamId) {
        Dream dream = findDreamByUser(dreamId, userId);
        dreamRepository.delete(dream);
        log.info("Dream deleted, ID: {}", dreamId);
    }

    // === 라이브러리 ===

    @Transactional
    public AddToLibraryResponse addToLibrary(Long userId, Long dreamId) {
        if (!subscriptionService.canAddToLibrary(userId)) {
            throw new BusinessException(ErrorCode.LIBRARY_LIMIT_EXCEEDED);
        }
        Dream dream = findDreamByUser(dreamId, userId);
        dream.addToLibrary();
        dreamRepository.save(dream);
        return AddToLibraryResponse.builder().dreamId(dreamId).isInLibrary(true).build();
    }

    @Transactional
    public ToggleFavoriteResponse toggleFavorite(Long userId, Long dreamId) {
        Dream dream = findDreamByUser(dreamId, userId);
        boolean willBeFavorite = !dream.getIsFavorite();
        if (willBeFavorite && !subscriptionService.canFavorite(userId)) {
            throw new BusinessException(ErrorCode.FAVORITE_LIMIT_EXCEEDED);
        }
        dream.toggleFavorite();
        dreamRepository.save(dream);
        return ToggleFavoriteResponse.builder()
                .dreamId(dreamId)
                .isFavorite(dream.getIsFavorite())
                .build();
    }

    public LibraryResponse getLibrary(
            Long userId,
            Boolean favorite,
            Genre genre,
            String search,
            String sort,
            Pageable pageable) {
        Page<Dream> dreams =
                dreamRepository.findLibraryDreams(
                        userId, favorite, genre, search, sort != null ? sort : "latest", pageable);
        var items =
                dreams.getContent().stream()
                        .map(
                                d ->
                                        LibraryItemResponse.builder()
                                                .dreamId(d.getId())
                                                .title(d.getTitle())
                                                .thumbnailUrl(
                                                        d.getWebtoonImages() != null
                                                                        && !d.getWebtoonImages()
                                                                                .isEmpty()
                                                                ? d.getWebtoonImages().get(0)
                                                                : null)
                                                .genre(d.getSelectedGenre())
                                                .isFavorite(d.getIsFavorite())
                                                .createdAt(d.getCreatedAt())
                                                .build())
                        .toList();
        return LibraryResponse.builder()
                .dreams(items)
                .totalCount(dreams.getTotalElements())
                .build();
    }

    private Dream findDreamByUser(Long dreamId, Long userId) {
        return dreamRepository
                .findByIdAndUserId(dreamId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));
    }
}
