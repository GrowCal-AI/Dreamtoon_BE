package com.dreamtoon.domain.dream.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상세 설명 입력 요청 (Step 3) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DreamDetailsRequest {

    @NotBlank(message = "상세 설명은 필수입니다")
    private String detailedDescription;

    /** 현실 고민 (선택) */
    private String realLifeContext;
}
