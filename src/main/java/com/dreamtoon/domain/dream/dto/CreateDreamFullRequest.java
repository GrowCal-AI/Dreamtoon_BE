package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** FE DreamInputForm과 1:1 대응하는 통합 꿈 생성 요청 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDreamFullRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "꿈 내용은 필수입니다")
    private String content;

    private List<String> characters;
    private List<String> location;
    private EmotionType mainEmotion;
    private String lastScene;

    @NotNull(message = "스타일 선택은 필수입니다")
    private Genre style;
}
