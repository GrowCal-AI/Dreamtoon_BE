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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Dreams",
        description =
                "**꿈 기록 → 감정 선택 → 상세 입력 → AI 분석 → 4컷 웹툰 생성** 전체 플로우를 제공하는 API입니다. 모든 API는"
                    + " **Authorization: Bearer {accessToken}** 헤더가 필요합니다. 호출 순서: 1) 꿈 기록 시작 → 2)"
                    + " 감정 선택 → 3) 상세 설명 입력 → 4) 분석 결과 조회(폴링) → 5) 웹툰 생성 → 6) 꿈 상세/라이브러리.")
@RestController
@RequestMapping("/api/v1/dreams")
@RequiredArgsConstructor
public class DreamController {

    private final DreamService dreamService;
    private final DreamChatService dreamChatService;

    @Operation(
            summary = "통합 꿈 생성 (FE용)",
            description =
                    "**[FE 전용]** 감정, 꿈 내용, 스타일을 한번에 전송합니다."
                            + " 비동기 AI 분석이 시작되며 processingStatus로 진행 상태를 확인하세요.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DreamResponse>> createDreamFull(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateDreamFullRequest request) {
        DreamResponse response = dreamService.createDreamFull(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "1. 꿈 기록 시작",
            description =
                    "**[화면: 메인] 꿈 기록 시작**\n\n"
                        + "사용자가 꿈 내용을 입력하고 전송 버튼을 누르면 호출합니다. 반환된 `dreamId`는 이후 감정 선택, 상세 입력, 결과 조회"
                        + " 등 모든 단계에서 사용되므로 프론트엔드에서 계속 가지고 있어야 합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<InitiateDreamResponse>> initiateDream(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InitiateDreamRequest request) {
        InitiateDreamResponse response = dreamService.initiateDream(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "2. 감정 선택",
            description =
                    "**[화면: 감정 선택]**\n\n"
                            + "꿈 기록 시작 후, 사용자가 6가지 감정 칩 중 하나를 선택했을 때 호출합니다. 요청 body에 감정 종류(`JOY`,"
                            + " `ANXIETY`, `ANGER`, `SADNESS`, `SURPRISE`, `PEACE`)를 보내주세요.")
    @PatchMapping("/{dreamId}/emotion")
    public ResponseEntity<ApiResponse<EmotionSelectResponse>> selectEmotion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "꿈 기록 시작 시 반환된 Dream ID") @PathVariable Long dreamId,
            @Valid @RequestBody EmotionSelectRequest request) {
        EmotionSelectResponse response = dreamService.selectEmotion(userId, dreamId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "3. 상세 설명 입력",
            description =
                    "**[화면: 상세 설명]**\n\n"
                        + "꿈에 대한 상세 내용과 현실 반영 질문(선택)을 입력하고 완료를 누르면 호출합니다. 이 API가 호출되면 백엔드에서 **비동기"
                        + " AI 분석**이 시작됩니다. 응답으로 `202 Accepted`를 받으면 로딩 화면을 띄우고, **GET"
                        + " /{dreamId}/analysis** API를 폴링해주세요.")
    @PatchMapping("/{dreamId}/details")
    public ResponseEntity<ApiResponse<DreamDetailsResponse>> addDetails(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "꿈 기록 ID") @PathVariable Long dreamId,
            @Valid @RequestBody DreamDetailsRequest request) {
        DreamDetailsResponse response = dreamService.addDetails(userId, dreamId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "4. 분석 결과 조회 (폴링용)",
            description =
                    "**[화면: 로딩 중] 분석 상태 폴링**\n\n"
                        + "상세 설명 입력(`PATCH /details`) 후, AI 분석이 완료되었는지 확인하기 위해 **2~3초 간격**으로 계속"
                        + " 호출해야 합니다. 응답의 `status`가 `ANALYSIS_COMPLETED`가 될 때까지 로딩을 유지하다가, 완료되면 결과"
                        + " 화면(장르 선택 전)을 보여주세요.")
    @GetMapping("/{dreamId}/analysis")
    public ResponseEntity<ApiResponse<DreamAnalysisResponse>> getAnalysis(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "꿈 기록 ID") @PathVariable Long dreamId) {
        DreamAnalysisResponse response = dreamService.getAnalysis(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "5. 4컷 만화 생성",
            description =
                    "**[화면: 장르 선택]**\n\n"
                        + "사용자가 웹툰 장르(`ROMANCE`, `FANTASY`, `HEALING`, `HORROR`)를 선택하면 호출합니다. 호출 즉시"
                        + " **비동기 이미지 생성**이 시작되며 `202 Accepted`가 반환됩니다. 이후 다시 로딩 화면을 띄우고, **GET"
                        + " /{dreamId}** (꿈 상세) API를 폴링하거나 완료 후 호출하여 최종 결과를 확인하세요.")
    @PostMapping("/{dreamId}/webtoon")
    public ResponseEntity<ApiResponse<WebtoonGenerateResponse>> generateWebtoon(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "꿈 기록 ID") @PathVariable Long dreamId,
            @Valid @RequestBody WebtoonGenerateRequest request) {
        WebtoonGenerateResponse response = dreamService.generateWebtoon(userId, dreamId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "꿈 상세 조회",
            description =
                    "**[화면: 결과 확인 / 라이브러리 상세]**\n\n"
                            + "꿈의 최종 결과(웹툰 이미지 포함)를 조회합니다. "
                            + "1. 웹툰 생성 중 로딩이 끝난 후 최종 결과를 보여줄 때\n"
                            + "2. 라이브러리 목록에서 아이템을 클릭해 상세 화면으로 들어갈 때 사용합니다.")
    @GetMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<DreamResponse>> getDream(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "꿈 기록 ID") @PathVariable Long dreamId) {
        DreamResponse response = dreamService.getDream(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내 꿈 목록 조회",
            description =
                    "**[화면: 마이페이지 / 내 꿈 기록]** (MVP 제외 가능)\n\n"
                            + "로그인한 사용자의 모든 꿈 기록을 최신순으로 조회합니다. "
                            + "라이브러리(`GET /library`)와 달리 '저장 안 함' 처리된 꿈까지 모두 포함된 히스토리입니다. "
                            + "MVP 단계에서 마이페이지가 없다면 사용하지 않아도 됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DreamResponse>>> getUserDreams(
            @AuthenticationPrincipal Long userId, @ModelAttribute PageRequest pageRequest) {
        PageResponse<DreamResponse> response =
                dreamService.getUserDreams(userId, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "꿈 삭제",
            description =
                    "**[화면: 상세 페이지 / 마이페이지] (선택 기능)**\n\n"
                            + "해당 꿈 기록을 영구 삭제합니다. "
                            + "MVP 단계에서는 필수 기능은 아니지만, 상세 페이지에 '삭제' 버튼이 있다면 연결하여 사용하세요. "
                            + "삭제 후에는 복구할 수 없습니다.")
    @DeleteMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<Void>> deleteDream(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "삭제할 꿈 기록 ID") @PathVariable Long dreamId) {
        dreamService.deleteDream(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success("꿈 기록이 삭제되었습니다."));
    }

    @Operation(
            summary = "라이브러리에 추가",
            description =
                    "꿈을 **내 라이브러리**에 저장합니다. 저장된 꿈은 **GET /api/v1/library** 에서 필터/검색과 함께 조회할 수 있습니다."
                            + " 이미 라이브러리에 있으면 `isInLibrary: true` 로 응답합니다.")
    @PostMapping("/{dreamId}/library")
    public ResponseEntity<ApiResponse<AddToLibraryResponse>> addToLibrary(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "라이브러리에 넣을 꿈 ID") @PathVariable Long dreamId) {
        AddToLibraryResponse response = dreamService.addToLibrary(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "즐겨찾기 토글",
            description =
                    "해당 꿈의 **즐겨찾기** 상태를 켜기/끄기 합니다. 한 번 호출할 때마다 on↔off가 바뀌며, 응답의 `isFavorite`로 현재"
                            + " 상태를 반영하세요. 라이브러리 조회 시 `favorite=true` 로 필터링 가능합니다.")
    @PatchMapping("/{dreamId}/favorite")
    public ResponseEntity<ApiResponse<ToggleFavoriteResponse>> toggleFavorite(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "즐겨찾기할 꿈 ID") @PathVariable Long dreamId) {
        ToggleFavoriteResponse response = dreamService.toggleFavorite(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "꿈 더 대화하기 (채팅 전송)",
            description =
                    "**[화면: 꿈 더 대화하기] 채팅 전송**\n\n"
                            + "결과 화면에서 '꿈 더 대화하기'를 눌러 진입한 채팅방에서 사용합니다. "
                            + "사용자가 메시지를 입력하고 전송하면, AI 페르소나가 꿈 내용에 기반한 답장을 줍니다.")
    @PostMapping("/{dreamId}/chat")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendChatMessage(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "대화할 꿈 ID") @PathVariable Long dreamId,
            @Valid @RequestBody SendChatRequest request) {
        ChatMessageResponse response = dreamChatService.sendMessage(userId, dreamId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "채팅 내역 조회",
            description =
                    "**[화면: 꿈 더 대화하기] 채팅 내역 로드**\n\n"
                            + "채팅방에 처음 진입했을 때 이전 대화 내용을 불러오기 위해 호출합니다. "
                            + "대화 내역을 시간순으로 정렬하여 채팅 UI에 표시해주면 됩니다.")
    @GetMapping("/{dreamId}/chat")
    public ResponseEntity<ApiResponse<ChatHistoryResponse>> getChatHistory(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "채팅 내역을 볼 꿈 ID") @PathVariable Long dreamId) {
        ChatHistoryResponse response = dreamChatService.getChatHistory(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
