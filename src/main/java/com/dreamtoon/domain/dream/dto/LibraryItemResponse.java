package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.Genre;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 라이브러리 목록 한 건 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryItemResponse {

    private Long dreamId;
    private String title;
    private String thumbnailUrl; // webtoonImages 첫 번째 또는 null
    private Genre genre;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
}
