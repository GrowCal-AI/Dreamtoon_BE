package com.dreamtoon.domain.scene.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 장면 대사 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueDto {

        private String character;
        private String text;
}
