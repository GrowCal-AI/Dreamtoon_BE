package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.analysis.repository.AnalysisRepository;
import com.dreamtoon.domain.dream.dto.CreateDreamRequest;
import com.dreamtoon.domain.dream.dto.DreamResponse;
import com.dreamtoon.domain.dream.dto.UpdateDreamRequest;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.scene.repository.SceneRepository;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.common.dto.response.PageResponse;
import com.dreamtoon.global.error.EntityNotFoundException;
import com.dreamtoon.global.error.ErrorCode;
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

        @Transactional
        public DreamResponse createDream(Long userId, CreateDreamRequest request) {
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

                // AI 처리 (동기 방식 - Phase 3에서 비동기로 전환 예정)
                // 1. GPT-4o로 장면 분할 및 감정 분석
                // 2. DALL-E 3로 각 장면 이미지 생성
                try {
                        dreamAiService.analyzeDream(dream);
                        log.info("Dream AI analysis completed for dream ID: {}", dream.getId());
                } catch (Exception e) {
                        log.error("Dream AI analysis failed, using fallback data", e);
                        // fallback은 DreamAiService 내부에서 처리됨
                }

                return DreamResponse.from(dream);
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
                                dream.toggleFavorite();
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

                dreamRepository.delete(dream);
        }
}
