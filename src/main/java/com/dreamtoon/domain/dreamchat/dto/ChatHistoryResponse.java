package com.dreamtoon.domain.dreamchat.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 채팅 내역 조회 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {

    private Long dreamId;
    private List<ChatMessageResponse> chatHistory;
}
