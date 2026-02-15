package com.dreamtoon.domain.scene.entity;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.scene.dto.DialogueDto;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_id", nullable = false)
    private Dream dream;

    @Column(name = "cut_order", nullable = false)
    private Integer cutOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> characters = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion")
    private EmotionType emotion;

    @Type(JsonType.class)
    @Column(name = "background_keywords", columnDefinition = "jsonb")
    private List<String> backgroundKeywords = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String narration;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<DialogueDto> dialogue = new ArrayList<>();

    @Builder
    public Scene(Dream dream, Integer cutOrder, String description, List<String> characters,
                 EmotionType emotion, List<String> backgroundKeywords, String imageUrl,
                 String narration, List<DialogueDto> dialogue) {
        this.dream = dream;
        this.cutOrder = cutOrder;
        this.description = description;
        this.characters = characters != null ? characters : new ArrayList<>();
        this.emotion = emotion;
        this.backgroundKeywords = backgroundKeywords != null ? backgroundKeywords : new ArrayList<>();
        this.imageUrl = imageUrl;
        this.narration = narration;
        this.dialogue = dialogue != null ? dialogue : new ArrayList<>();
    }

    public void setDream(Dream dream) {
        this.dream = dream;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
