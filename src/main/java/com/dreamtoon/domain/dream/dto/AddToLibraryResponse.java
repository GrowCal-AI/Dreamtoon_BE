package com.dreamtoon.domain.dream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 라이브러리 등록 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToLibraryResponse {

    private Long dreamId;
    private Boolean isInLibrary;
}
