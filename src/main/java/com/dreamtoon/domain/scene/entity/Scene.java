package com.dreamtoon.domain.scene.entity;

import com.dreamtoon.domain.dream.entity.Dream;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String dialogue;
    
    @Builder
    public Scene(Dream dream, Integer cutOrder, String description, String imageUrl, String dialogue) {
        this.dream = dream;
        this.cutOrder = cutOrder;
        this.description = description;
        this.imageUrl = imageUrl;
        this.dialogue = dialogue;
    }
    
    public void setDream(Dream dream) {
        this.dream = dream;
    }
    
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
