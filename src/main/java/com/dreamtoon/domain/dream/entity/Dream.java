package com.dreamtoon.domain.dream.entity;

import com.dreamtoon.domain.user.entity.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    // === 사용자 입력 데이터 ===

    @Column(name = "dream_content", nullable = false, columnDefinition = "TEXT")
    private String dreamContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_emotion")
    private EmotionType primaryEmotion;

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "real_life_context", columnDefinition = "TEXT")
    private String realLifeContext;

    // === AI 생성 데이터 ===

    @Column private String title;

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;

    @Type(JsonType.class)
    @Column(name = "emotion_scores", columnDefinition = "jsonb")
    private Map<String, Integer> emotionScores = new HashMap<>();

    @Column(name = "ai_insight", columnDefinition = "TEXT")
    private String aiInsight;

    // === 웹툰 관련 ===

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_genre")
    private Genre selectedGenre;

    @Type(JsonType.class)
    @Column(name = "webtoon_images", columnDefinition = "jsonb")
    private List<String> webtoonImages = new ArrayList<>();

    // === 라이브러리 관련 ===

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "is_in_library", nullable = false)
    private Boolean isInLibrary = false;

    // === 처리 상태 ===

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // === 타임스탬프 ===

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Dream(User user, String dreamContent) {
        this.user = user;
        this.dreamContent = dreamContent;
        this.processingStatus = ProcessingStatus.PENDING;
        this.isFavorite = false;
        this.isInLibrary = false;
        this.emotionScores = new HashMap<>();
        this.webtoonImages = new ArrayList<>();
    }

    // === 단계별 비즈니스 메서드 ===

    /** Step 2: 감정 선택 */
    public void selectEmotion(EmotionType emotion) {
        this.primaryEmotion = emotion;
    }

    /** Step 3: 상세 설명 추가 */
    public void addDetails(String detailedDescription, String realLifeContext) {
        this.detailedDescription = detailedDescription;
        this.realLifeContext = realLifeContext;
    }

    /** AI 분석 시작 */
    public void startAnalyzing() {
        this.processingStatus = ProcessingStatus.ANALYZING;
    }

    /** AI 분석 완료 */
    public void completeAnalysis(
            String title, String aiAnalysis, Map<String, Integer> emotionScores, String aiInsight) {
        this.title = title;
        this.aiAnalysis = aiAnalysis;
        this.emotionScores = emotionScores;
        this.aiInsight = aiInsight;
        this.processingStatus = ProcessingStatus.ANALYSIS_COMPLETED;
        this.errorMessage = null;
    }

    /** Step 5: 장르 선택 */
    public void selectGenre(Genre genre) {
        this.selectedGenre = genre;
    }

    /** 웹툰 생성 시작 */
    public void startGenerating() {
        this.processingStatus = ProcessingStatus.GENERATING;
    }

    /** 웹툰 생성 완료 */
    public void completeGeneration(List<String> webtoonImages) {
        this.webtoonImages = webtoonImages;
        this.processingStatus = ProcessingStatus.COMPLETED;
        this.errorMessage = null;
    }

    /** 처리 실패 */
    public void failProcessing(String errorMessage) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    // === 라이브러리 메서드 ===

    /** 라이브러리에 추가 */
    public void addToLibrary() {
        this.isInLibrary = true;
    }

    /** 라이브러리에서 제거 */
    public void removeFromLibrary() {
        this.isInLibrary = false;
    }

    /** 즐겨찾기 토글 */
    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }
}
