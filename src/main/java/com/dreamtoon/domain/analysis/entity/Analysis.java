package com.dreamtoon.domain.analysis.entity;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.EmotionType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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
        private Map<EmotionType, Double> emotions;

        @Column(name = "tension_level")
        private Integer tensionLevel;

        @Column(name = "control_level")
        private Integer controlLevel;

        @Column(name = "is_nightmare")
        private Boolean isNightmare;

        @Type(JsonType.class)
        @Column(name = "repeating_symbols", columnDefinition = "jsonb")
        private List<String> repeatingSymbols = new ArrayList<>();

        @Type(JsonType.class)
        @Column(name = "relationship_patterns", columnDefinition = "jsonb")
        private List<String> relationshipPatterns = new ArrayList<>();

        @Column(name = "has_resolution")
        private Boolean hasResolution;

        @Column(name = "ai_insight", columnDefinition = "TEXT")
        private String aiInsight;

        @Builder
        public Analysis(
                        Dream dream,
                        Integer healthScore,
                        Map<EmotionType, Double> emotions,
                        Integer tensionLevel,
                        Integer controlLevel,
                        Boolean isNightmare,
                        List<String> repeatingSymbols,
                        List<String> relationshipPatterns,
                        Boolean hasResolution,
                        String aiInsight) {
                this.dream = dream;
                this.healthScore = healthScore;
                this.emotions = emotions;
                this.tensionLevel = tensionLevel;
                this.controlLevel = controlLevel;
                this.isNightmare = isNightmare;
                this.repeatingSymbols = repeatingSymbols != null ? repeatingSymbols : new ArrayList<>();
                this.relationshipPatterns =
                                relationshipPatterns != null ? relationshipPatterns : new ArrayList<>();
                this.hasResolution = hasResolution;
                this.aiInsight = aiInsight;
        }

        public void setDream(Dream dream) {
                this.dream = dream;
        }
}
