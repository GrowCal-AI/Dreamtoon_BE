package com.dreamtoon.domain.dream.controller;

import com.dreamtoon.domain.dream.dto.*;
import com.dreamtoon.domain.dream.service.GuestDreamService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 비회원 꿈 미리보기 API.
 *
 * <p>인증 불필요. 최초 생성 시 반환된 {@code guestToken}을 이후 모든 요청의 {@code ?token=} 파라미터로 전달해야 한다.
 */
@Tag(
        name = "Guest Dreams",
        description =
                "**비회원 체험용** 꿈 분석·웹툰 생성 API. 인증 불필요."
                        + " 생성 시 받은 `guestToken`을 이후 모든 요청에 `?token=` 파라미터로 전달하세요."
                        + " 프리미엄 스타일은 사용할 수 없습니다.")
@RestController
@RequestMapping("/api/v1/guest/dreams")
@RequiredArgsConstructor
public class GuestDreamController {

    private final GuestDreamService guestDreamService;

    @Operation(
            summary = "비회원 꿈 생성",
            description =
                    "로그인 없이 꿈을 생성하고 AI 분석을 시작합니다."
                            + " 응답의 `guestToken`을 저장하여 이후 폴링·웹툰 생성·조회에 사용하세요.")
    @PostMapping
    public ResponseEntity<ApiResponse<GuestDreamCreateResponse>> createGuestDream(
            @Valid @RequestBody GuestDreamCreateRequest request) {
        GuestDreamCreateResponse response = guestDreamService.createGuestDream(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "비회원 분석 결과 조회 (폴링용)",
            description =
                    "AI 분석 완료 여부를 확인합니다. `status`가 `ANALYSIS_COMPLETED`가 될 때까지 2~3초 간격으로 폴링하세요.")
    @GetMapping("/{dreamId}/analysis")
    public ResponseEntity<ApiResponse<DreamAnalysisResponse>> getAnalysis(
            @Parameter(description = "꿈 ID") @PathVariable Long dreamId,
            @Parameter(description = "생성 시 발급받은 guestToken", required = true) @RequestParam
                    String token) {
        DreamAnalysisResponse response = guestDreamService.getAnalysis(dreamId, token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "비회원 웹툰 생성",
            description =
                    "분석 완료(`ANALYSIS_COMPLETED`) 후 웹툰 생성을 시작합니다."
                            + " 스탠다드 스타일만 선택 가능합니다 (프리미엄 불가).")
    @PostMapping("/{dreamId}/webtoon")
    public ResponseEntity<ApiResponse<WebtoonGenerateResponse>> generateWebtoon(
            @Parameter(description = "꿈 ID") @PathVariable Long dreamId,
            @Parameter(description = "생성 시 발급받은 guestToken", required = true) @RequestParam
                    String token,
            @Valid @RequestBody WebtoonGenerateRequest request) {
        WebtoonGenerateResponse response =
                guestDreamService.generateWebtoon(dreamId, token, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "비회원 꿈 상세 조회",
            description = "최종 결과(웹툰 이미지 포함)를 조회합니다. 웹툰 생성 완료 폴링 후 호출하세요.")
    @GetMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<DreamResponse>> getDream(
            @Parameter(description = "꿈 ID") @PathVariable Long dreamId,
            @Parameter(description = "생성 시 발급받은 guestToken", required = true) @RequestParam
                    String token) {
        DreamResponse response = guestDreamService.getDream(dreamId, token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
