package com.dreamtoon.domain.conversation.entity;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.user.entity.User;
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

/** 대화 세션 - 사용자와 AI 에이전트의 대화를 관리 */
@Entity
@Table(
                name = "conversations",
                indexes = {@Index(name = "idx_conversations_user_id", columnList = "user_id")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Conversation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "dream_id")
        private Dream dream; // 웹툰 생성 후 연결됨

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ConversationStatus status = ConversationStatus.IN_PROGRESS;

        @Enumerated(EnumType.STRING)
        @Column(name = "current_phase", nullable = false)
        private ConversationPhase currentPhase = ConversationPhase.EMOTION_COLLECTION;

        @Type(JsonType.class)
        @Column(name = "collected_data", columnDefinition = "jsonb")
        private Map<String, Object> collectedData = new HashMap<>();

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Builder
        public Conversation(User user) {
                this.user = user;
                this.status = ConversationStatus.IN_PROGRESS;
                this.currentPhase = ConversationPhase.EMOTION_COLLECTION;
                this.collectedData = new HashMap<>();
        }

        // === 비즈니스 메서드 ===

        /** 다음 단계로 진행 */
        public void advanceToNextPhase() {
                switch (this.currentPhase) {
                        case EMOTION_COLLECTION:
                                this.currentPhase = ConversationPhase.CONTEXT_ANALYSIS;
                                break;
                        case CONTEXT_ANALYSIS:
                                this.currentPhase = ConversationPhase.REPORT_GENERATION;
                                break;
                        case REPORT_GENERATION:
                                this.currentPhase = ConversationPhase.CONTENT_CREATION;
                                break;
                        case CONTENT_CREATION:
                                // 마지막 단계에서는 더 이상 진행하지 않음
                                break;
                }
        }

        /** 특정 데이터 저장 */
        public void storeData(String key, Object value) {
                if (this.collectedData == null) {
                        this.collectedData = new HashMap<>();
                }
                this.collectedData.put(key, value);
        }

        /** 특정 데이터 조회 */
        @SuppressWarnings("unchecked")
        public <T> T getData(String key, Class<T> type) {
                if (this.collectedData == null) {
                        return null;
                }
                Object value = this.collectedData.get(key);
                if (value == null) {
                        return null;
                }
                return (T) value;
        }

        /** 대화 완료 */
        public void complete(Dream dream) {
                this.status = ConversationStatus.COMPLETED;
                this.dream = dream;
        }

        /** 대화 중단 */
        public void abandon() {
                this.status = ConversationStatus.ABANDONED;
        }

        /** 진행 중인 대화인지 확인 */
        public boolean isInProgress() {
                return this.status == ConversationStatus.IN_PROGRESS;
        }

        /** 감정 수집 단계인지 확인 */
        public boolean isEmotionCollectionPhase() {
                return this.currentPhase == ConversationPhase.EMOTION_COLLECTION;
        }

        /** 상황 분석 단계인지 확인 */
        public boolean isContextAnalysisPhase() {
                return this.currentPhase == ConversationPhase.CONTEXT_ANALYSIS;
        }

        /** 리포트 생성 단계인지 확인 */
        public boolean isReportGenerationPhase() {
                return this.currentPhase == ConversationPhase.REPORT_GENERATION;
        }

        /** 콘텐츠 생성 단계인지 확인 */
        public boolean isContentCreationPhase() {
                return this.currentPhase == ConversationPhase.CONTENT_CREATION;
        }
}
