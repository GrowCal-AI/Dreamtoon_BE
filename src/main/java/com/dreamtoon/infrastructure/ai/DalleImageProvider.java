package com.dreamtoon.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;

/**
 * DALL-E 3 이미지 생성 프로바이더 (기존 Spring AI ImageClient 래핑).
 * application.yml의 image.provider=dalle 일 때 활성화.
 */
@Slf4j
@RequiredArgsConstructor
public class DalleImageProvider implements ImageGenerationProvider {

    private final ImageClient imageClient;

    @Override
    public String generateImage(String prompt) {
        log.info("[DALL-E 3] Generating image...");

        OpenAiImageOptions options =
                OpenAiImageOptions.builder()
                        .withModel("dall-e-3")
                        .withQuality("standard")
                        .withN(1)
                        .withWidth(1024)
                        .withHeight(1792)
                        .build();

        ImagePrompt imagePrompt = new ImagePrompt(prompt, options);
        ImageResponse response = imageClient.call(imagePrompt);

        String imageUrl = response.getResult().getOutput().getUrl();
        log.info("[DALL-E 3] Image generated: {}", imageUrl);
        return imageUrl;
    }

    @Override
    public String getProviderName() {
        return "DALL-E 3";
    }
}
