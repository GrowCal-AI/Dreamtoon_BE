package com.dreamtoon.domain.dream.controller;

import com.dreamtoon.domain.dream.dto.LibraryResponse;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.service.DreamService;
import com.dreamtoon.global.common.dto.request.PageRequest;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Library", description = "라이브러리(저장한 꿈) 조회 API")
@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final DreamService dreamService;

    @Operation(
            summary = "라이브러리 조회",
            description = "저장한 꿈 목록을 필터링하여 조회합니다. favorite, genre, search, sort 지원.")
    @GetMapping
    public ResponseEntity<ApiResponse<LibraryResponse>> getLibrary(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @ModelAttribute PageRequest pageRequest) {
        LibraryResponse response =
                dreamService.getLibrary(
                        userId, favorite, genre, search, sort, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
