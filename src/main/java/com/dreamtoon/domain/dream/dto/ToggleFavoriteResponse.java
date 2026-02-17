package com.dreamtoon.domain.dream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 즐겨찾기 토글 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleFavoriteResponse {

    private Long dreamId;
    private Boolean isFavorite;
}
