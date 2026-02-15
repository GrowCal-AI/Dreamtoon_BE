package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.analysis.repository.AnalysisRepository;
import com.dreamtoon.domain.dream.dto.CreateDreamRequest;
import com.dreamtoon.domain.dream.dto.DreamResponse;
import com.dreamtoon.domain.dream.dto.DreamStatusResponse;
import com.dreamtoon.domain.dream.dto.UpdateDreamRequest;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.StylePreset;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.scene.repository.SceneRepository;
import com.dreamtoon.domain.subscription.service.SubscriptionService;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.common.dto.response.PageResponse;
import com.dreamtoon.global.error.BusinessException;
import com.dreamtoon.global.error.EntityNotFoundException;
import com.dreamtoon.global.error.ErrorCode;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DreamService {

    private final DreamRepository dreamRepository;
    private final UserRepository userRepository;
    private final SceneRepository sceneRepository;
    private final AnalysisRepository analysisRepository;
    private final DreamAiService dreamAiService;
    private final SubscriptionService subscriptionService;

    // 프리미엄 전용 스타일
    private static final List<StylePreset> PREMIUM_STYLES =
            Arrays.asList(
                    StylePreset.DARK_FANTASY,
                    StylePreset.FANTASY,
                    StylePreset.HORROR,
                    StylePreset.SD_REFRAME);

    @Transactional
    public DreamResponse createDream(Long userId, CreateDreamRequest request) {
        // 1. 구독 제한 확인 - 생성 가능 여부
        if (!subscriptionService.canGenerate(userId)) {
            throw new BusinessException(ErrorCode.GENERATION_LIMIT_EXCEEDED);
        }

        // 2. 프리미엄 스타일 접근 권한 확인
        if (PREMIUM_STYLES.contains(request.getStyle())
                && !subscriptionService.canUsePremiumStyles(userId)) {
            throw new BusinessException(ErrorCode.PREMIUM_STYLE_NOT_ALLOWED);
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Dream dream =
                Dream.builder()
                        .user(user)
                        .title(request.getTitle())
                        .rawContent(request.getContent())
                        .stylePreset(request.getStyle())
                        .inputMethod(request.getInputMethod())
                        .build();

        dreamRepository.save(dream);

        // 3. 생성 횟수 증가 (생성 성공 후)
        subscriptionService.incrementGenerationCount(userId);

        // 4. 비동기로 AI 처리 시작 (즉시 응답 반환)
        dreamAiService.analyzeDreamAsync(dream.getId());
        log.info("Dream created with ID: {}, async processing started", dream.getId());

        return DreamResponse.from(dream);
    }

    public DreamStatusResponse getDreamStatus(Long dreamId) {
        Dream dream =
                dreamRepository
                        .findById(dreamId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));

        return DreamStatusResponse.from(dream);
    }

    public DreamResponse getDream(Long dreamId) {
        Dream dream = dreamRepository.findByIdWithDetails(dreamId);
        if (dream == null) {
            throw new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND);
        }

        return DreamResponse.fromWithDetails(dream);
    }

    public PageResponse<DreamResponse> getUserDreams(Long userId, Pageable pageable) {
        Page<Dream> dreams = dreamRepository.findByUserId(userId, pageable);
        Page<DreamResponse> dreamResponses = dreams.map(DreamResponse::from);

        return PageResponse.of(dreamResponses);
    }

    @Transactional
    public DreamResponse updateDream(Long userId, Long dreamId, UpdateDreamRequest request) {
        Dream dream =
                dreamRepository
                        .findById(dreamId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));

        if (!dream.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 업데이트 가능한 필드들
        if (request.getTitle() != null) {
            dream.updateTitle(request.getTitle());
        }

        if (request.getTags() != null) {
            dream.updateTags(request.getTags());
        }

        if (request.getIsFavorite() != null) {
            if (request.getIsFavorite() != dream.getIsFavorite()) {
                // 즐겨찾기 추가 시 저장 제한 확인
                if (request.getIsFavorite() && !subscriptionService.canSave(userId)) {
                    throw new BusinessException(ErrorCode.SAVE_LIMIT_EXCEEDED);
                }

                dream.toggleFavorite();

                // 즐겨찾기 상태에 따라 저장 카운트 조정
                if (dream.getIsFavorite()) {
                    subscriptionService.incrementSavedCount(userId);
                    log.info("Dream ID: {} added to favorites", dreamId);
                } else {
                    subscriptionService.decrementSavedCount(userId);
                    log.info("Dream ID: {} removed from favorites", dreamId);
                }
            }
        }

        return DreamResponse.from(dream);
    }

    @Transactional
    public void deleteDream(Long userId, Long dreamId) {
        Dream dream =
                dreamRepository
                        .findById(dreamId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));

        if (!dream.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 즐겨찾기된 꿈이면 저장 카운트 감소
        if (dream.getIsFavorite()) {
            subscriptionService.decrementSavedCount(userId);
            log.info("Decremented saved count due to dream deletion, ID: {}", dreamId);
        }

        dreamRepository.delete(dream);
        log.info("Dream deleted, ID: {}", dreamId);
    }

    /**
     * 사용 가능한 스타일 목록 조회 (구독 티어에 따라 필터링)
     *
     * @param userId 사용자 ID
     * @return 스타일 목록 응답
     */
    public com.dreamtoon.domain.dream.dto.StyleListResponse getAvailableStyles(Long userId) {
        boolean hasPremiumAccess = subscriptionService.canUsePremiumStyles(userId);

        List<com.dreamtoon.domain.dream.dto.StyleOptionResponse> styles =
                Arrays.stream(StylePreset.values())
                        .map(
                                preset -> {
                                    boolean isPremium = PREMIUM_STYLES.contains(preset);
                                    boolean isAccessible = !isPremium || hasPremiumAccess;
                                    return com.dreamtoon.domain.dream.dto.StyleOptionResponse.of(
                                            preset, isPremium, isAccessible);
                                })
                        .toList();

        return com.dreamtoon.domain.dream.dto.StyleListResponse.builder()
                .styles(styles)
                .hasPremiumAccess(hasPremiumAccess)
                .build();
    }
}
