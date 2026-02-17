package com.dreamtoon.domain.dreamchat.entity;

import com.dreamtoon.domain.dream.entity.Dream;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 꿈 더 대화하기 - 심리상담 채팅 메시지 */
@Entity
@Table(
        name = "dream_chats",
        indexes = {@Index(name = "idx_dream_chats_dream_id", columnList = "dream_id")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DreamChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_id", nullable = false)
    private Dream dream;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DreamChat(Dream dream, ChatRole role, String message) {
        this.dream = dream;
        this.role = role;
        this.message = message;
    }
}
