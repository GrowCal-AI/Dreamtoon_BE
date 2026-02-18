package com.dreamtoon.domain.dream.controller;

import com.dreamtoon.domain.dream.dto.LibraryResponse;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.service.DreamService;
import com.dreamtoon.global.common.dto.request.PageRequest;
import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Library",
        description =
                "**라이브러리**는 사용자가 '라이브러리에 추가'한 꿈만 모아서 보는 공간입니다. 즐겨찾기/장르/검색/정렬 필터를 지원하며,"
                        + " **Authorization: Bearer {accessToken}** 이 필요합니다.")
@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final DreamService dreamService;

    @Operation(
            summary = "라이브러리 목록 조회",
            description =
                    "**저장한 꿈** 목록을 페이징·필터링하여 조회합니다. **쿼리 파라미터**: `favorite`(true=즐겨찾기만),"
                            + " `genre`(ROMANCE|FANTASY|HEALING|HORROR), `search`(제목/내용 검색어),"
                            + " `sort`(기본값 latest=최신순). 페이지네이션은 `page`, `size` 로 지정하세요.")
    @GetMapping
    public ResponseEntity<ApiResponse<LibraryResponse>> getLibrary(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "true면 즐겨찾기한 꿈만") @RequestParam(required = false)
                    Boolean favorite,
            @Parameter(description = "장르로 필터") @RequestParam(required = false) Genre genre,
            @Parameter(description = "제목/내용 검색어") @RequestParam(required = false) String search,
            @Parameter(description = "정렬: latest(최신순) 등")
                    @RequestParam(required = false, defaultValue = "latest")
                    String sort,
            @ModelAttribute PageRequest pageRequest) {
        LibraryResponse response =
                dreamService.getLibrary(
                        userId, favorite, genre, search, sort, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
