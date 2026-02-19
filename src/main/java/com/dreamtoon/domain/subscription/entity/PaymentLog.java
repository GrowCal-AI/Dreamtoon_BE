package com.dreamtoon.domain.subscription.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 결제 이벤트 감사 로그 */
@Entity
@Table(
        name = "payment_logs",
        indexes = {
            @Index(name = "idx_payment_log_user_id", columnList = "user_id"),
            @Index(name = "idx_payment_log_event_type", columnList = "event_type"),
            @Index(name = "idx_payment_log_created_at", columnList = "created_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private PaymentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier")
    private SubscriptionTier tier;

    @Column(name = "polar_subscription_id")
    private String polarSubscriptionId;

    @Column(name = "polar_checkout_id")
    private String polarCheckoutId;

    @Column(name = "polar_customer_id")
    private String polarCustomerId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PaymentLog(
            Long userId,
            Long subscriptionId,
            PaymentEventType eventType,
            SubscriptionTier tier,
            String polarSubscriptionId,
            String polarCheckoutId,
            String polarCustomerId,
            String description,
            Map<String, Object> metadata) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.eventType = eventType;
        this.tier = tier;
        this.polarSubscriptionId = polarSubscriptionId;
        this.polarCheckoutId = polarCheckoutId;
        this.polarCustomerId = polarCustomerId;
        this.description = description;
        this.metadata = metadata;
    }
}
