package com.dreamtoon.domain.dream.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dream 업데이트 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdateDreamRequest {

    private String title;
    private List<String> tags;
    private Boolean isFavorite;
}
