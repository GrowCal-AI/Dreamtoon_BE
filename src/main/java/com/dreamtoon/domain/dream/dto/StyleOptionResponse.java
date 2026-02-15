package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.StylePreset;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StyleOptionResponse {

    private StylePreset preset;
    private String name;
    private String description;
    private Boolean isPremium;
    private Boolean isAccessible;

    public static StyleOptionResponse of(
            StylePreset preset, boolean isPremium, boolean isAccessible) {
        return StyleOptionResponse.builder()
                .preset(preset)
                .name(preset.getDescription())
                .description(preset.getPromptTemplate())
                .isPremium(isPremium)
                .isAccessible(isAccessible)
                .build();
    }
}
