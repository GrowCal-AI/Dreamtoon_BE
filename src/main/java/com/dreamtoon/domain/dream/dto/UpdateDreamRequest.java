package com.dreamtoon.domain.dream.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Dream 업데이트 요청 DTO */
@Getter
@NoArgsConstructor
public class UpdateDreamRequest {

        private String title;
        private List<String> tags;
        private Boolean isFavorite;
}
