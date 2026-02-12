package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.analysis.entity.Analysis;
import com.dreamtoon.domain.analysis.repository.AnalysisRepository;
import com.dreamtoon.domain.dream.dto.CreateDreamRequest;
import com.dreamtoon.domain.dream.dto.DreamResponse;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.scene.entity.Scene;
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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DreamService {
    
    private final DreamRepository dreamRepository;
    private final UserRepository userRepository;
    private final SceneRepository sceneRepository;
    private final AnalysisRepository analysisRepository;
    // private final DreamAiService dreamAiService; // TODO: Spring AI 서비스 추가
    
    @Transactional
    public DreamResponse createDream(Long userId, CreateDreamRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        
        Dream dream = Dream.builder()
                .user(user)
                .rawContent(request.getContent())
                .stylePreset(request.getStyle())
                .build();
        
        dreamRepository.save(dream);
        
        // TODO: Spring AI를 통한 비동기 처리
        // 1. GPT로 장면 분할
        // 2. DALL-E로 이미지 생성
        // 3. 감정 분석 및 DHI 점수 산출
        
        // 임시 데이터 생성 (나중에 AI 서비스로 대체)
        createMockScenesAndAnalysis(dream);
        
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
    public void deleteDream(Long userId, Long dreamId) {
        Dream dream = dreamRepository.findById(dreamId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));
        
        if (!dream.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        
        dreamRepository.delete(dream);
    }
    
    // TODO: AI 서비스 구현 후 제거할 임시 메서드
    private void createMockScenesAndAnalysis(Dream dream) {
        // Mock 장면 생성
        for (int i = 1; i <= 4; i++) {
            Scene scene = Scene.builder()
                    .dream(dream)
                    .cutOrder(i)
                    .description("장면 " + i + " 설명")
                    .imageUrl("https://example.com/scene" + i + ".png")
                    .dialogue("대사 " + i)
                    .build();
            dream.addScene(scene);
        }
        
        // Mock 분석 생성
        Map<String, Double> emotions = new HashMap<>();
        emotions.put("joy", 0.3);
        emotions.put("fear", 0.1);
        emotions.put("calm", 0.6);
        
        Analysis analysis = Analysis.builder()
                .dream(dream)
                .healthScore(75)
                .emotions(emotions)
                .aiInsight("편안한 꿈을 꾸셨네요! 정서적으로 안정된 상태입니다.")
                .build();
        
        dream.setAnalysis(analysis);
    }
}
