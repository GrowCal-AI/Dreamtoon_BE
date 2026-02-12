package com.dreamtoon.domain.dream.entity;

import com.dreamtoon.domain.analysis.entity.Analysis;
import com.dreamtoon.domain.scene.entity.Scene;
import com.dreamtoon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dreams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Dream {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "raw_content", nullable = false, columnDefinition = "TEXT")
    private String rawContent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "style_preset", nullable = false)
    private StylePreset stylePreset;
    
    @OneToMany(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("cutOrder ASC")
    private List<Scene> scenes = new ArrayList<>();
    
    @OneToOne(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Analysis analysis;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public Dream(User user, String rawContent, StylePreset stylePreset) {
        this.user = user;
        this.rawContent = rawContent;
        this.stylePreset = stylePreset;
    }
    
    public void addScene(Scene scene) {
        this.scenes.add(scene);
        scene.setDream(this);
    }
    
    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
        analysis.setDream(this);
    }
}
