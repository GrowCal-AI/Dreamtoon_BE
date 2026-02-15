package com.dreamtoon.domain.dream.entity;

import com.dreamtoon.domain.analysis.entity.Analysis;
import com.dreamtoon.domain.scene.entity.Scene;
import com.dreamtoon.domain.user.entity.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
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

    @Column(nullable = false)
    private String title;

    @Column(name = "raw_content", nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "style_preset", nullable = false)
    private StylePreset stylePreset;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_method", nullable = false)
    private InputMethod inputMethod = InputMethod.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "webtoon_url")
    private String webtoonUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @OneToMany(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("cutOrder ASC")
    private List<Scene> scenes = new ArrayList<>();

    @OneToOne(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Analysis analysis;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Dream(User user, String title, String rawContent, StylePreset stylePreset,
            InputMethod inputMethod, List<String> tags, Boolean isFavorite,
            String webtoonUrl, String videoUrl, LocalDateTime recordedAt) {
        this.user = user;
        this.title = title;
        this.rawContent = rawContent;
        this.stylePreset = stylePreset;
        this.inputMethod = inputMethod != null ? inputMethod : InputMethod.TEXT;
        this.processingStatus = ProcessingStatus.PENDING;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.isFavorite = isFavorite != null ? isFavorite : false;
        this.webtoonUrl = webtoonUrl;
        this.videoUrl = videoUrl;
        this.recordedAt = recordedAt != null ? recordedAt : LocalDateTime.now();
    }

    public void addScene(Scene scene) {
        this.scenes.add(scene);
        scene.setDream(this);
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
        analysis.setDream(this);
    }

    // 업데이트 메서드들
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public void updateTags(List<String> tags) {
        this.tags = tags;
    }

    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }

    public void setWebtoonUrl(String webtoonUrl) {
        this.webtoonUrl = webtoonUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // 상태 관리 메서드들
    public void startProcessing() {
        this.processingStatus = ProcessingStatus.PROCESSING;
    }

    public void completeProcessing() {
        this.processingStatus = ProcessingStatus.COMPLETED;
        this.errorMessage = null;
    }

    public void failProcessing(String errorMessage) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
