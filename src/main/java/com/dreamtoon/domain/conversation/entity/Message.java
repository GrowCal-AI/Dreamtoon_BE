package com.dreamtoon.domain.conversation.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 대화 메시지 */
@Entity
@Table(
                name = "messages",
                indexes = {@Index(name = "idx_messages_conversation_id", columnList = "conversation_id")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Message {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "conversation_id", nullable = false)
        private Conversation conversation;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private MessageRole role;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String content;

        @Type(JsonType.class)
        @Column(columnDefinition = "jsonb")
        private Map<String, Object> metadata = new HashMap<>();

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Builder
        public Message(
                        Conversation conversation,
                        MessageRole role,
                        String content,
                        Map<String, Object> metadata) {
                this.conversation = conversation;
                this.role = role;
                this.content = content;
                this.metadata = metadata != null ? metadata : new HashMap<>();
        }

        // === 비즈니스 메서드 ===

        /** 메타데이터 추가 */
        public void addMetadata(String key, Object value) {
                if (this.metadata == null) {
                        this.metadata = new HashMap<>();
                }
                this.metadata.put(key, value);
        }

        /** 사용자 메시지인지 확인 */
        public boolean isUserMessage() {
                return this.role == MessageRole.USER;
        }

        /** AI 메시지인지 확인 */
        public boolean isAssistantMessage() {
                return this.role == MessageRole.ASSISTANT;
        }

        /** 시스템 메시지인지 확인 */
        public boolean isSystemMessage() {
                return this.role == MessageRole.SYSTEM;
        }
}
