package com.dreamtoon.domain.scene.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.scene.entity.Scene;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SceneResponse {

    private Long sceneId;
    private Integer sceneNumber; // cutOrder를 sceneNumber로 변경 (프론트엔드와 일치)
    private String description;
    private List<String> characters;
    private EmotionType emotion;
    private List<String> backgroundKeywords;
    private String imageUrl;
    private String narration;
    private List<DialogueDto> dialogue;

    public static SceneResponse from(Scene scene) {
        return SceneResponse.builder()
                .sceneId(scene.getId())
                .sceneNumber(scene.getCutOrder())
                .description(scene.getDescription())
                .characters(scene.getCharacters())
                .emotion(scene.getEmotion())
                .backgroundKeywords(scene.getBackgroundKeywords())
                .imageUrl(scene.getImageUrl())
                .narration(scene.getNarration())
                .dialogue(scene.getDialogue())
                .build();
    }
}
