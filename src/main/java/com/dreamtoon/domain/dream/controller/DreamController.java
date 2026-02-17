package com.dreamtoon.domain.dream.controller;

import com.dreamtoon.domain.dream.dto.*;
import com.dreamtoon.domain.dream.service.DreamService;
import com.dreamtoon.domain.dreamchat.dto.ChatHistoryResponse;
import com.dreamtoon.domain.dreamchat.dto.ChatMessageResponse;
import com.dreamtoon.domain.dreamchat.dto.SendChatRequest;
import com.dreamtoon.domain.dreamchat.service.DreamChatService;
import com.dreamtoon.global.common.dto.request.PageRequest;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import com.dreamtoon.global.common.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dreams", description = "꿈 기록 및 4컷 웹툰 생성 API (Blueprint v2.0)")
@RestController
@RequestMapping("/api/v1/dreams")
@RequiredArgsConstructor
public class DreamController {

    private final DreamService dreamService;
    private final DreamChatService dreamChatService;

    @Operation(summary = "꿈 기록 시작", description = "꿈 내용을 입력하고 Dream을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<InitiateDreamResponse>> initiateDream(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InitiateDreamRequest request) {
        InitiateDreamResponse response = dreamService.initiateDream(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "감정 선택", description = "꿈에서 느낀 주요 감정을 선택합니다.")
    @PatchMapping("/{dreamId}/emotion")
    public ResponseEntity<ApiResponse<EmotionSelectResponse>> selectEmotion(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dreamId,
            @Valid @RequestBody EmotionSelectRequest request) {
        EmotionSelectResponse response = dreamService.selectEmotion(userId, dreamId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상세 설명 입력", description = "상세 설명을 저장하고 비동기로 AI 분석을 시작합니다.")
    @PatchMapping("/{dreamId}/details")
    public ResponseEntity<ApiResponse<DreamDetailsResponse>> addDetails(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dreamId,
            @Valid @RequestBody DreamDetailsRequest request) {
        DreamDetailsResponse response = dreamService.addDetails(userId, dreamId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }

    @Operation(summary = "분석 결과 조회", description = "AI 꿈 분석 결과를 조회합니다. (폴링 가능)")
    @GetMapping("/{dreamId}/analysis")
    public ResponseEntity<ApiResponse<DreamAnalysisResponse>> getAnalysis(
            @AuthenticationPrincipal Long userId, @PathVariable Long dreamId) {
        DreamAnalysisResponse response = dreamService.getAnalysis(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "4컷 만화 생성", description = "장르를 선택하고 비동기로 웹툰 이미지 생성을 시작합니다.")
    @PostMapping("/{dreamId}/webtoon")
    public ResponseEntity<ApiResponse<WebtoonGenerateResponse>> generateWebtoon(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dreamId,
            @Valid @RequestBody WebtoonGenerateRequest request) {
        WebtoonGenerateResponse response = dreamService.generateWebtoon(userId, dreamId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }

    @Operation(summary = "꿈 상세 조회", description = "꿈 전체 정보(분석, 웹툰 이미지 포함)를 조회합니다.")
    @GetMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<DreamResponse>> getDream(
            @AuthenticationPrincipal Long userId, @PathVariable Long dreamId) {
        DreamResponse response = dreamService.getDream(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 꿈 목록 조회", description = "로그인한 사용자의 꿈 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DreamResponse>>> getUserDreams(
            @AuthenticationPrincipal Long userId, @ModelAttribute PageRequest pageRequest) {
        PageResponse<DreamResponse> response =
                dreamService.getUserDreams(userId, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "꿈 삭제", description = "꿈 기록을 삭제합니다.")
    @DeleteMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<Void>> deleteDream(
            @AuthenticationPrincipal Long userId, @PathVariable Long dreamId) {
        dreamService.deleteDream(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success("꿈 기록이 삭제되었습니다."));
    }

    @Operation(summary = "라이브러리에 추가", description = "꿈을 라이브러리에 저장합니다.")
    @PostMapping("/{dreamId}/library")
    public ResponseEntity<ApiResponse<AddToLibraryResponse>> addToLibrary(
            @AuthenticationPrincipal Long userId, @PathVariable Long dreamId) {
        AddToLibraryResponse response = dreamService.addToLibrary(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "즐겨찾기 토글", description = "꿈의 즐겨찾기 상태를 토글합니다.")
    @PatchMapping("/{dreamId}/favorite")
    public ResponseEntity<ApiResponse<ToggleFavoriteResponse>> toggleFavorite(
            @AuthenticationPrincipal Long userId, @PathVariable Long dreamId) {
        ToggleFavoriteResponse response = dreamService.toggleFavorite(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "꿈 더 대화하기", description = "심리상담사 챗봇과 꿈에 대해 대화합니다.")
    @PostMapping("/{dreamId}/chat")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendChatMessage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dreamId,
            @Valid @RequestBody SendChatRequest request) {
        ChatMessageResponse response = dreamChatService.sendMessage(userId, dreamId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "채팅 내역 조회", description = "해당 꿈의 심리상담 채팅 내역을 조회합니다.")
    @GetMapping("/{dreamId}/chat")
    public ResponseEntity<ApiResponse<ChatHistoryResponse>> getChatHistory(
            @AuthenticationPrincipal Long userId, @PathVariable Long dreamId) {
        ChatHistoryResponse response = dreamChatService.getChatHistory(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
