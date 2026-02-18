package com.dreamtoon.domain.analytics.controller;

import com.dreamtoon.domain.analytics.dto.EmotionAnalysisResponse;
import com.dreamtoon.domain.analytics.dto.HealthIndexResponse;
import com.dreamtoon.domain.analytics.dto.PatternAnalysisResponse;
import com.dreamtoon.domain.analytics.service.AnalyticsService;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics", description = "꿈 분석 통계 API (건강 지수, 감정 분포, 패턴)")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "건강 지수 조회", description = "사용자의 전체 꿈 데이터 기반 건강 지수를 조회합니다.")
    @GetMapping("/health-index")
    public ResponseEntity<ApiResponse<HealthIndexResponse>> getHealthIndex(
            @AuthenticationPrincipal Long userId) {
        HealthIndexResponse response = analyticsService.getHealthIndex(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "감정 분석 조회", description = "기간별 감정 분포 통계를 조회합니다. period: week, month, year")
    @GetMapping("/emotions")
    public ResponseEntity<ApiResponse<EmotionAnalysisResponse>> getEmotionAnalysis(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "month") String period) {
        EmotionAnalysisResponse response = analyticsService.getEmotionAnalysis(userId, period);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "꿈 패턴 분석", description = "반복되는 상징, 관계 패턴 등을 분석합니다.")
    @GetMapping("/patterns")
    public ResponseEntity<ApiResponse<PatternAnalysisResponse>> getDreamPatterns(
            @AuthenticationPrincipal Long userId) {
        PatternAnalysisResponse response = analyticsService.getDreamPatterns(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
