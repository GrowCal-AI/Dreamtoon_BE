package com.dreamtoon.domain.analysis.controller;

import com.dreamtoon.domain.analysis.dto.DreamAnalysisResponse;
import com.dreamtoon.domain.analysis.service.DreamAnalysisService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis", description = "꿈 분석 대시보드 및 통계 관련 API")
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class DreamAnalysisController {

    private final DreamAnalysisService dreamAnalysisService;

    @Operation(
            summary = "꿈 분석 대시보드 조회",
            description =
                    "사용자의 최근 꿈 기록을 바탕으로 **스트레스 지수, 수면 품질, 감정 균형, 주간 흐름** 등을 종합 분석하여 제공합니다. "
                            + "데이터가 없는 경우 `hasEnoughData: false`를 반환합니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DreamAnalysisResponse>> getDashboard(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회 기간 (일 단위, 기본값 7)") @RequestParam(defaultValue = "7")
                    int period) {
        DreamAnalysisResponse response = dreamAnalysisService.getDashboardAnalysis(userId, period);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
