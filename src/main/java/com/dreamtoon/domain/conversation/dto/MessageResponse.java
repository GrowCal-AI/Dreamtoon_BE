package com.dreamtoon.domain.conversation.dto;

import com.dreamtoon.domain.conversation.entity.Message;
import com.dreamtoon.domain.conversation.entity.MessageRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

        private Long messageId;
        private MessageRole role;
        private String content;
        private Map<String, Object> metadata;
        private LocalDateTime createdAt;

        public static MessageResponse from(Message message) {
                return MessageResponse.builder()
                                .messageId(message.getId())
                                .role(message.getRole())
                                .content(message.getContent())
                                .metadata(message.getMetadata())
                                .createdAt(message.getCreatedAt())
                                .build();
        }
}
