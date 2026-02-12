package com.dreamtoon.domain.scene.dto;

import com.dreamtoon.domain.scene.entity.Scene;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SceneResponse {
    
    private Long sceneId;
    private Integer cutOrder;
    private String description;
    private String imageUrl;
    private String dialogue;
    
    public static SceneResponse from(Scene scene) {
        return SceneResponse.builder()
                .sceneId(scene.getId())
                .cutOrder(scene.getCutOrder())
                .description(scene.getDescription())
                .imageUrl(scene.getImageUrl())
                .dialogue(scene.getDialogue())
                .build();
    }
}
