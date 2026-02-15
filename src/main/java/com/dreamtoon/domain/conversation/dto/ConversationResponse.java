package com.dreamtoon.domain.conversation.dto;

import com.dreamtoon.domain.conversation.entity.Conversation;
import com.dreamtoon.domain.conversation.entity.ConversationPhase;
import com.dreamtoon.domain.conversation.entity.ConversationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {

        private Long conversationId;
        private Long userId;
        private Long dreamId;
        private ConversationStatus status;
        private ConversationPhase currentPhase;
        private Map<String, Object> collectedData;
        private LocalDateTime createdAt;

        public static ConversationResponse from(Conversation conversation) {
                return ConversationResponse.builder()
                                .conversationId(conversation.getId())
                                .userId(conversation.getUser().getId())
                                .dreamId(conversation.getDream() != null ? conversation.getDream().getId() : null)
                                .status(conversation.getStatus())
                                .currentPhase(conversation.getCurrentPhase())
                                .collectedData(conversation.getCollectedData())
                                .createdAt(conversation.getCreatedAt())
                                .build();
        }
}
