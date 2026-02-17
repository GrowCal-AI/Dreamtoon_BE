package com.dreamtoon.domain.dream.dto;

import com.dreamtoon.domain.dream.entity.Genre;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 4컷 만화 생성 요청 (Step 5: 장르 선택 후) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonGenerateRequest {

    @NotNull(message = "장르 선택은 필수입니다")
    private Genre selectedGenre;
}
