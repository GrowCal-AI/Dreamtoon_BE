package com.dreamtoon.domain.dream.controller;

import com.dreamtoon.domain.dream.dto.CreateDreamRequest;
import com.dreamtoon.domain.dream.dto.DreamResponse;
import com.dreamtoon.domain.dream.service.DreamService;
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

@Tag(name = "Dreams", description = "꿈 기록 및 웹툰 생성 API")
@RestController
@RequestMapping("/api/v1/dreams")
@RequiredArgsConstructor
public class DreamController {
    
    private final DreamService dreamService;
    
    @Operation(summary = "꿈 기록 생성", description = "사용자의 꿈을 기록하고 AI 웹툰 생성을 시작합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<DreamResponse>> createDream(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateDreamRequest request
    ) {
        DreamResponse response = dreamService.createDream(userId, request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("꿈 기록이 생성되었습니다. AI 웹툰 생성이 진행 중입니다.", response));
    }
    
    @Operation(summary = "꿈 상세 조회", description = "생성된 웹툰 및 분석 데이터를 조회합니다.")
    @GetMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<DreamResponse>> getDream(
            @PathVariable Long dreamId
    ) {
        DreamResponse response = dreamService.getDream(dreamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "내 꿈 목록 조회", description = "로그인한 사용자의 꿈 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DreamResponse>>> getUserDreams(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute PageRequest pageRequest
    ) {
        PageResponse<DreamResponse> response = dreamService.getUserDreams(userId, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "꿈 삭제", description = "꿈 기록을 삭제합니다.")
    @DeleteMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<Void>> deleteDream(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dreamId
    ) {
        dreamService.deleteDream(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.success("꿈 기록이 삭제되었습니다."));
    }
}
