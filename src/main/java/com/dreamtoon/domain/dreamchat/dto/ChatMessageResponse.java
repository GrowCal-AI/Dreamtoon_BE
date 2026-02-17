package com.dreamtoon.domain.dreamchat.dto;

import com.dreamtoon.domain.dreamchat.entity.ChatRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 채팅 메시지 한 건 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private ChatRole role;
    private String message;
    private LocalDateTime createdAt;
}
