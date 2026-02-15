package com.dreamtoon.domain.conversation.service;

import com.dreamtoon.domain.conversation.dto.*;
import com.dreamtoon.domain.conversation.entity.*;
import com.dreamtoon.domain.conversation.repository.ConversationRepository;
import com.dreamtoon.domain.conversation.repository.MessageRepository;
import com.dreamtoon.domain.conversation.repository.StressAssessmentRepository;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.global.error.BusinessException;
import com.dreamtoon.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 대화 관리 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

        private final ConversationRepository conversationRepository;
        private final MessageRepository messageRepository;
        private final StressAssessmentRepository stressAssessmentRepository;
        private final UserRepository userRepository;

        /**
         * 새 대화 시작
         *
         * @param userId 사용자 ID
         * @return 대화 응답
         */
        @Transactional
        public ConversationResponse startConversation(Long userId) {
                User user =
                                userRepository
                                                .findById(userId)
                                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                // 기존 진행 중인 대화가 있으면 반환
                return conversationRepository
                                .findInProgressConversation(userId)
                                .map(ConversationResponse::from)
                                .orElseGet(
                                                () -> {
                                                        // 새 대화 생성
                                                        Conversation conversation = Conversation.builder().user(user).build();
                                                        conversationRepository.save(conversation);

                                                        // 환영 메시지 생성
                                                        Message welcomeMessage =
                                                                        Message.builder()
                                                                                        .conversation(conversation)
                                                                                        .role(MessageRole.ASSISTANT)
                                                                                        .content(
                                                                                                        "안녕하세요! 오늘 꿈에서 어떤 감정을 느끼셨나요? 아래에서 가장 가까운 감정을"
                                                                                                                        + " 선택해주세요.")
                                                                                        .build();
                                                        messageRepository.save(welcomeMessage);

                                                        log.info(
                                                                        "Started new conversation ID: {} for user ID: {}",
                                                                        conversation.getId(),
                                                                        userId);
                                                        return ConversationResponse.from(conversation);
                                                });
        }

        /**
         * 대화 조회
         *
         * @param conversationId 대화 ID
         * @return 대화 응답
         */
        public ConversationResponse getConversation(Long conversationId) {
                Conversation conversation =
                                conversationRepository
                                                .findById(conversationId)
                                                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));
                return ConversationResponse.from(conversation);
        }

        /**
         * 대화의 모든 메시지 조회
         *
         * @param conversationId 대화 ID
         * @return 메시지 목록
         */
        public List<MessageResponse> getMessages(Long conversationId) {
                // 대화 존재 여부 확인
                conversationRepository
                                .findById(conversationId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));

                return messageRepository.findByConversationId(conversationId).stream()
                                .map(MessageResponse::from)
                                .collect(Collectors.toList());
        }

        /**
         * 메시지 전송
         *
         * @param conversationId 대화 ID
         * @param request 메시지 요청
         * @return 메시지 응답
         */
        @Transactional
        public MessageResponse sendMessage(Long conversationId, SendMessageRequest request) {
                Conversation conversation =
                                conversationRepository
                                                .findById(conversationId)
                                                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));

                // 완료된 대화에는 메시지를 보낼 수 없음
                if (!conversation.isInProgress()) {
                        throw new BusinessException(ErrorCode.CONVERSATION_ALREADY_COMPLETED);
                }

                // 사용자 메시지 저장
                Message userMessage =
                                Message.builder()
                                                .conversation(conversation)
                                                .role(MessageRole.USER)
                                                .content(request.getContent())
                                                .build();
                messageRepository.save(userMessage);

                // TODO: AI 응답 생성 로직 (Phase 2에서 구현)
                // 현재는 단순 확인 메시지 반환
                Message assistantMessage =
                                Message.builder()
                                                .conversation(conversation)
                                                .role(MessageRole.ASSISTANT)
                                                .content("메시지를 받았습니다. AI 응답 생성 기능은 곧 구현될 예정입니다.")
                                                .build();
                messageRepository.save(assistantMessage);

                log.info("User message saved for conversation ID: {}", conversationId);
                return MessageResponse.from(assistantMessage);
        }

        /**
         * 감정 선택 처리
         *
         * @param conversationId 대화 ID
         * @param request 감정 선택 요청
         * @return 감정 요약 응답
         */
        @Transactional
        public EmotionSummaryResponse selectEmotions(
                        Long conversationId, EmotionSelectionRequest request) {
                Conversation conversation =
                                conversationRepository
                                                .findById(conversationId)
                                                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));

                // 현재 단계 확인
                if (!conversation.isEmotionCollectionPhase()) {
                        throw new BusinessException(ErrorCode.INVALID_CONVERSATION_PHASE);
                }

                // 감정 데이터 저장
                conversation.storeData("emotions", request.getEmotionIntensities());

                // 주요 감정 찾기
                Map<EmotionType, Integer> emotions = request.getEmotionIntensities();
                EmotionType dominantEmotion =
                                emotions.entrySet().stream()
                                                .max(Map.Entry.comparingByValue())
                                                .map(Map.Entry::getKey)
                                                .orElse(EmotionType.JOY);

                Integer dominantIntensity = emotions.get(dominantEmotion);

                // 다음 단계로 진행
                conversation.advanceToNextPhase();
                conversationRepository.save(conversation);

                // AI 해석 생성 (간단한 규칙 기반, Phase 3에서 GPT로 개선)
                String interpretation = generateSimpleInterpretation(dominantEmotion, dominantIntensity);

                log.info(
                                "Emotions selected for conversation ID: {}, dominant: {}",
                                conversationId,
                                dominantEmotion);

                return EmotionSummaryResponse.builder()
                                .dominantEmotion(dominantEmotion)
                                .dominantIntensity(dominantIntensity)
                                .aiInterpretation(interpretation)
                                .build();
        }

        /**
         * 스트레스 평가 제출
         *
         * @param conversationId 대화 ID
         * @param request 스트레스 평가 요청
         * @return 스트레스 평가 응답
         */
        @Transactional
        public StressAssessmentResponse submitStressAssessment(
                        Long conversationId, StressAssessmentRequest request) {
                Conversation conversation =
                                conversationRepository
                                                .findById(conversationId)
                                                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));

                // 현재 단계 확인
                if (!conversation.isContextAnalysisPhase()) {
                        throw new BusinessException(ErrorCode.INVALID_CONVERSATION_PHASE);
                }

                // 스트레스 평가 저장
                StressAssessment assessment =
                                StressAssessment.builder()
                                                .conversation(conversation)
                                                .workStress(request.getWorkStress())
                                                .relationshipStress(request.getRelationshipStress())
                                                .healthStress(request.getHealthStress())
                                                .financialStress(request.getFinancialStress())
                                                .sleepQuality(request.getSleepQuality())
                                                .stressFactors(request.getStressFactors())
                                                .build();
                stressAssessmentRepository.save(assessment);

                // 다음 단계로 진행
                conversation.advanceToNextPhase();
                conversationRepository.save(conversation);

                // AI 권장사항 생성 (간단한 규칙 기반, Phase 3에서 GPT로 개선)
                String recommendation = generateSimpleRecommendation(assessment);

                log.info(
                                "Stress assessment submitted for conversation ID: {}, total index: {}",
                                conversationId,
                                assessment.calculateTotalStressIndex());

                return StressAssessmentResponse.from(assessment, recommendation);
        }

        /**
         * 대화 삭제
         *
         * @param conversationId 대화 ID
         * @param userId 사용자 ID
         */
        @Transactional
        public void deleteConversation(Long conversationId, Long userId) {
                Conversation conversation =
                                conversationRepository
                                                .findById(conversationId)
                                                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND));

                // 권한 확인
                if (!conversation.getUser().getId().equals(userId)) {
                        throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
                }

                conversationRepository.delete(conversation);
                log.info("Deleted conversation ID: {} for user ID: {}", conversationId, userId);
        }

        // === 유틸리티 메서드 ===

        /** 간단한 감정 해석 생성 (Phase 3에서 GPT로 교체) */
        private String generateSimpleInterpretation(EmotionType emotion, Integer intensity) {
                String intensityLevel = intensity >= 70 ? "강하게" : intensity >= 40 ? "보통으로" : "약하게";

                return switch (emotion) {
                        case JOY -> intensityLevel + " 기쁨을 느끼셨군요. 긍정적인 꿈이었네요!";
                        case ANXIETY -> intensityLevel + " 불안감을 느끼셨네요. 현실의 걱정이 반영된 것 같습니다.";
                        case ANGER -> intensityLevel + " 분노를 느끼셨군요. 해결되지 않은 갈등이 있으신가요?";
                        case SADNESS -> intensityLevel + " 슬픔을 느끼셨네요. 마음이 힘드셨던 것 같습니다.";
                        case SURPRISE -> intensityLevel + " 놀라움을 느끼셨군요. 예상치 못한 일이 있었나요?";
                        case PEACE -> intensityLevel + " 평온함을 느끼셨네요. 마음이 안정되어 있으신 것 같습니다.";
                };
        }

        /** 간단한 권장사항 생성 (Phase 3에서 GPT로 교체) */
        private String generateSimpleRecommendation(StressAssessment assessment) {
                List<String> topStressors = assessment.getTopStressors();
                int totalStress = assessment.calculateTotalStressIndex();

                if (totalStress >= 70) {
                        return "스트레스 수준이 높습니다. " + String.join("과 ", topStressors) + " 부분에서 휴식이 필요해 보입니다.";
                } else if (totalStress >= 40) {
                        return "보통 수준의 스트레스입니다. " + String.join("과 ", topStressors) + " 부분을 개선해보세요.";
                } else {
                        return "스트레스 수준이 낮습니다. 현재 상태를 잘 유지하고 계시네요!";
                }
        }
}
