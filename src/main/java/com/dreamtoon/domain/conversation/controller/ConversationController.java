package com.dreamtoon.domain.conversation.controller;

import com.dreamtoon.domain.conversation.dto.*;
import com.dreamtoon.domain.conversation.service.ConversationService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Conversations", description = "대화형 AI 에이전트 API")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

        private final ConversationService conversationService;

        @Operation(
                        summary = "대화 시작",
                        description = "AI 에이전트와의 새로운 대화를 시작합니다. 진행 중인 대화가 있으면 해당 대화를 반환합니다.")
        @PostMapping
        public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
                        @AuthenticationPrincipal Long userId) {
                ConversationResponse response = conversationService.startConversation(userId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("대화가 시작되었습니다.", response));
        }

        @Operation(summary = "대화 조회", description = "특정 대화의 정보를 조회합니다.")
        @GetMapping("/{conversationId}")
        public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
                        @PathVariable Long conversationId) {
                ConversationResponse response = conversationService.getConversation(conversationId);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "메시지 목록 조회", description = "대화의 모든 메시지를 시간순으로 조회합니다.")
        @GetMapping("/{conversationId}/messages")
        public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
                        @PathVariable Long conversationId) {
                List<MessageResponse> response = conversationService.getMessages(conversationId);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "메시지 전송", description = "대화에 사용자 메시지를 전송하고 AI 응답을 받습니다.")
        @PostMapping("/{conversationId}/messages")
        public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
                        @PathVariable Long conversationId, @Valid @RequestBody SendMessageRequest request) {
                MessageResponse response = conversationService.sendMessage(conversationId, request);
                return ResponseEntity.ok(ApiResponse.success("메시지가 전송되었습니다.", response));
        }

        @Operation(summary = "감정 선택", description = "꿈에서 느낀 감정과 강도를 선택합니다. (감정 수집 단계)")
        @PostMapping("/{conversationId}/emotions/select")
        public ResponseEntity<ApiResponse<EmotionSummaryResponse>> selectEmotions(
                        @PathVariable Long conversationId,
                        @Valid @RequestBody EmotionSelectionRequest request) {
                EmotionSummaryResponse response =
                                conversationService.selectEmotions(conversationId, request);
                return ResponseEntity.ok(ApiResponse.success("감정이 분석되었습니다.", response));
        }

        @Operation(summary = "스트레스 평가", description = "현재 스트레스 상태를 평가합니다. (상황 분석 단계)")
        @PostMapping("/{conversationId}/stress")
        public ResponseEntity<ApiResponse<StressAssessmentResponse>> submitStressAssessment(
                        @PathVariable Long conversationId,
                        @Valid @RequestBody StressAssessmentRequest request) {
                StressAssessmentResponse response =
                                conversationService.submitStressAssessment(conversationId, request);
                return ResponseEntity.ok(ApiResponse.success("스트레스 평가가 완료되었습니다.", response));
        }

        @Operation(summary = "대화 삭제", description = "대화와 관련된 모든 메시지를 삭제합니다.")
        @DeleteMapping("/{conversationId}")
        public ResponseEntity<ApiResponse<Void>> deleteConversation(
                        @AuthenticationPrincipal Long userId, @PathVariable Long conversationId) {
                conversationService.deleteConversation(conversationId, userId);
                return ResponseEntity.ok(ApiResponse.success("대화가 삭제되었습니다."));
        }
}
