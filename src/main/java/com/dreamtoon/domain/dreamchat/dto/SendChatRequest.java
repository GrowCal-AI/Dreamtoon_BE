package com.dreamtoon.domain.dreamchat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 꿈 더 대화하기 요청 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendChatRequest {

    @NotBlank(message = "메시지 내용은 필수입니다")
    private String message;
}
