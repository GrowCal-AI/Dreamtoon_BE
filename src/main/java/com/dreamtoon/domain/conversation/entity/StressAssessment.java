package com.dreamtoon.domain.conversation.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 스트레스 평가 */
@Entity
@Table(name = "stress_assessments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class StressAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "work_stress")
    private Integer workStress; // 0-100

    @Column(name = "relationship_stress")
    private Integer relationshipStress; // 0-100

    @Column(name = "health_stress")
    private Integer healthStress; // 0-100

    @Column(name = "financial_stress")
    private Integer financialStress; // 0-100

    @Column(name = "sleep_quality")
    private Integer sleepQuality; // 0-100

    @Type(JsonType.class)
    @Column(name = "stress_factors", columnDefinition = "jsonb")
    private List<String> stressFactors = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public StressAssessment(
            Conversation conversation,
            Integer workStress,
            Integer relationshipStress,
            Integer healthStress,
            Integer financialStress,
            Integer sleepQuality,
            List<String> stressFactors) {
        this.conversation = conversation;
        this.workStress = workStress;
        this.relationshipStress = relationshipStress;
        this.healthStress = healthStress;
        this.financialStress = financialStress;
        this.sleepQuality = sleepQuality;
        this.stressFactors = stressFactors != null ? stressFactors : new ArrayList<>();
    }

    // === 비즈니스 메서드 ===

    /** 총 스트레스 지수 계산 (0-100) */
    public int calculateTotalStressIndex() {
        int sum = 0;
        int count = 0;

        if (workStress != null) {
            sum += workStress;
            count++;
        }
        if (relationshipStress != null) {
            sum += relationshipStress;
            count++;
        }
        if (healthStress != null) {
            sum += healthStress;
            count++;
        }
        if (financialStress != null) {
            sum += financialStress;
            count++;
        }

        return count > 0 ? sum / count : 0;
    }

    /** 최상위 스트레스 요인 조회 (상위 2개) */
    public List<String> getTopStressors() {
        List<StressFactor> factors = new ArrayList<>();

        if (workStress != null) {
            factors.add(new StressFactor("업무", workStress));
        }
        if (relationshipStress != null) {
            factors.add(new StressFactor("관계", relationshipStress));
        }
        if (healthStress != null) {
            factors.add(new StressFactor("건강", healthStress));
        }
        if (financialStress != null) {
            factors.add(new StressFactor("재정", financialStress));
        }

        factors.sort((a, b) -> Integer.compare(b.value, a.value));

        return factors.stream().limit(2).map(f -> f.name).toList();
    }

    /** 수면의 질 평가 */
    public String getSleepQualityLevel() {
        if (sleepQuality == null) {
            return "평가되지 않음";
        }
        if (sleepQuality >= 80) {
            return "양호";
        }
        if (sleepQuality >= 50) {
            return "보통";
        }
        return "불량";
    }

    /** 스트레스 요인 내부 클래스 */
    private static class StressFactor {
        String name;
        int value;

        StressFactor(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
