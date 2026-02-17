package com.dreamtoon.domain.dream.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 꿈 기록 시작 요청 (Step 1: 꿈 내용 입력) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateDreamRequest {

    @NotBlank(message = "꿈 내용은 필수입니다")
    private String dreamContent;
}
