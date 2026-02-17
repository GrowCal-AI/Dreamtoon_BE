package com.dreamtoon.domain.dreamchat.service;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.dreamchat.dto.ChatHistoryResponse;
import com.dreamtoon.domain.dreamchat.dto.ChatMessageResponse;
import com.dreamtoon.domain.dreamchat.dto.SendChatRequest;
import com.dreamtoon.domain.dreamchat.entity.ChatRole;
import com.dreamtoon.domain.dreamchat.entity.DreamChat;
import com.dreamtoon.domain.dreamchat.repository.DreamChatRepository;
import com.dreamtoon.global.error.EntityNotFoundException;
import com.dreamtoon.global.error.ErrorCode;
import com.dreamtoon.infrastructure.ai.OpenAiClient;
import com.dreamtoon.infrastructure.ai.prompt.DreamChatPrompt;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 꿈 더 대화하기 (심리상담 챗봇) 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DreamChatService {

    private final DreamRepository dreamRepository;
    private final DreamChatRepository dreamChatRepository;
    private final OpenAiClient openAiClient;

    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long dreamId, SendChatRequest request) {
        Dream dream =
                dreamRepository
                        .findByIdAndUserId(dreamId, userId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));

        // 사용자 메시지 저장
        DreamChat userChat =
                DreamChat.builder()
                        .dream(dream)
                        .role(ChatRole.USER)
                        .message(request.getMessage())
                        .build();
        dreamChatRepository.save(userChat);

        // GPT 호출 (심리상담사 페르소나)
        String systemPrompt =
                DreamChatPrompt.createSystemPrompt(dream.getDreamContent(), dream.getAiAnalysis());
        String aiContent = openAiClient.chatWithSystem(systemPrompt, request.getMessage());

        // AI 응답 저장
        DreamChat assistantChat =
                DreamChat.builder()
                        .dream(dream)
                        .role(ChatRole.ASSISTANT)
                        .message(aiContent)
                        .build();
        dreamChatRepository.save(assistantChat);

        return ChatMessageResponse.builder()
                .role(ChatRole.ASSISTANT)
                .message(aiContent)
                .createdAt(assistantChat.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ChatHistoryResponse getChatHistory(Long userId, Long dreamId) {
        Dream dream =
                dreamRepository
                        .findByIdAndUserId(dreamId, userId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DREAM_NOT_FOUND));

        List<DreamChat> chats = dreamChatRepository.findByDreamIdOrderByCreatedAtAsc(dreamId);
        List<ChatMessageResponse> history =
                chats.stream()
                        .map(
                                c ->
                                        ChatMessageResponse.builder()
                                                .role(c.getRole())
                                                .message(c.getMessage())
                                                .createdAt(c.getCreatedAt())
                                                .build())
                        .toList();

        return ChatHistoryResponse.builder().dreamId(dreamId).chatHistory(history).build();
    }
}
