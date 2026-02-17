package com.dreamtoon.domain.dream.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 라이브러리 조회 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryResponse {

    private List<LibraryItemResponse> dreams;
    private long totalCount;
}
