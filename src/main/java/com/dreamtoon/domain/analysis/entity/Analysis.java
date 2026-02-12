package com.dreamtoon.domain.analysis.entity;

import com.dreamtoon.domain.dream.entity.Dream;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Analysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_id", nullable = false, unique = true)
    private Dream dream;
    
    @Column(name = "health_score", nullable = false)
    private Integer healthScore;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> emotions;
    
    @Column(name = "ai_insight", columnDefinition = "TEXT")
    private String aiInsight;
    
    @Builder
    public Analysis(Dream dream, Integer healthScore, Map<String, Double> emotions, String aiInsight) {
        this.dream = dream;
        this.healthScore = healthScore;
        this.emotions = emotions;
        this.aiInsight = aiInsight;
    }
    
    public void setDream(Dream dream) {
        this.dream = dream;
    }
}
