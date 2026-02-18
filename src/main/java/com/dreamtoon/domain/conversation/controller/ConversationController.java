package com.dreamtoon.domain.conversation.controller;

import com.dreamtoon.domain.conversation.dto.*;
import com.dreamtoon.domain.conversation.service.ConversationService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Hidden
@Tag(
        name = "Conversations",
        description =
                "**대화형 AI 에이전트**와의 대화를 관리하는 API입니다. 대화 시작 → 메시지 주고받기 → (선택) 감정 선택·스트레스 평가 단계가 있습니다."
                        + " 모든 API에 **Authorization: Bearer {accessToken}** 이 필요합니다.")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(
            summary = "대화 시작",
            description =
                    "**새 대화**를 시작합니다. 이미 진행 중인 대화가 있으면 그 대화 정보를 반환하고, 없으면 새로 생성 후 반환합니다. "
                            + "응답의 `conversationId`로 이후 메시지 전송·조회 API를 호출하세요.")
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @AuthenticationPrincipal Long userId) {
        ConversationResponse response = conversationService.startConversation(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("대화가 시작되었습니다.", response));
    }

    @Operation(
            summary = "대화 조회",
            description =
                    "`conversationId`에 해당하는 **대화 메타 정보**를 조회합니다. 대화 목록에서 한 건을 선택했을 때 상세 진입 전에 호출하면"
                            + " 됩니다.")
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @Parameter(description = "대화 ID") @PathVariable Long conversationId) {
        ConversationResponse response = conversationService.getConversation(conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "메시지 목록 조회",
            description = "해당 대화의 **모든 메시지**를 **시간순**으로 조회합니다. 채팅 화면 진입 시 과거 대화 내역을 불러올 때 사용하세요.")
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @Parameter(description = "대화 ID") @PathVariable Long conversationId) {
        List<MessageResponse> response = conversationService.getMessages(conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "메시지 전송",
            description =
                    "사용자 메시지를 보내고 **AI 응답**을 바로 받습니다. 요청 body에 `content`(메시지 텍스트)를 넣어 보내면 "
                            + "AI 응답 메시지가 응답에 포함되므로, 채팅 UI에 사용자 메시지와 AI 메시지를 순서대로 추가하면 됩니다.")
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Parameter(description = "대화 ID") @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = conversationService.sendMessage(conversationId, request);
        return ResponseEntity.ok(ApiResponse.success("메시지가 전송되었습니다.", response));
    }

    @Operation(
            summary = "감정 선택",
            description =
                    "**감정 수집 단계**에서 사용합니다. 꿈에서 느낀 감정과 강도를 선택해 보내면, "
                            + "AI가 감정 요약·인사이트를 반환합니다. 대화 플로우 중 '감정 선택' 화면에서 호출하세요.")
    @PostMapping("/{conversationId}/emotions/select")
    public ResponseEntity<ApiResponse<EmotionSummaryResponse>> selectEmotions(
            @Parameter(description = "대화 ID") @PathVariable Long conversationId,
            @Valid @RequestBody EmotionSelectionRequest request) {
        EmotionSummaryResponse response =
                conversationService.selectEmotions(conversationId, request);
        return ResponseEntity.ok(ApiResponse.success("감정이 분석되었습니다.", response));
    }

    @Operation(
            summary = "스트레스 평가",
            description =
                    "**상황 분석 단계**에서 사용합니다. 사용자의 현재 스트레스 수준/항목을 보내면 "
                            + "평가 결과가 반환됩니다. 스트레스 체크 UI 제출 시 이 API를 호출하세요.")
    @PostMapping("/{conversationId}/stress")
    public ResponseEntity<ApiResponse<StressAssessmentResponse>> submitStressAssessment(
            @Parameter(description = "대화 ID") @PathVariable Long conversationId,
            @Valid @RequestBody StressAssessmentRequest request) {
        StressAssessmentResponse response =
                conversationService.submitStressAssessment(conversationId, request);
        return ResponseEntity.ok(ApiResponse.success("스트레스 평가가 완료되었습니다.", response));
    }

    @Operation(
            summary = "대화 삭제",
            description = "해당 대화와 **연결된 모든 메시지**를 삭제합니다. 본인이 소유한 대화만 삭제할 수 있으며, 삭제 후 복구할 수 없습니다.")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "삭제할 대화 ID") @PathVariable Long conversationId) {
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.ok(ApiResponse.success("대화가 삭제되었습니다."));
    }
}
