package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.StylePreset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateDreamRequest {
    
    @NotBlank(message = "꿈 내용을 입력해주세요.")
    private String content;
    
    @NotNull(message = "스타일을 선택해주세요.")
    private StylePreset style;
}
