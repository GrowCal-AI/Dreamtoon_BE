package com.dreamtoon.domain.dream.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StyleListResponse {

    private List<StyleOptionResponse> styles;
    private Boolean hasPremiumAccess;
}
