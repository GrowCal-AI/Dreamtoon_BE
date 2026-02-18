package com.dreamtoon.global.config;

import com.dreamtoon.infrastructure.ai.DalleImageProvider;
import com.dreamtoon.infrastructure.ai.GptImageProvider;
import com.dreamtoon.infrastructure.ai.ImageGenerationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 이미지 생성 프로바이더 설정.
 * image.provider 프로퍼티에 따라 활성 프로바이더 결정.
 *
 * <ul>
 *   <li>dalle: DALL-E 3 (Spring AI ImageClient)</li>
 *   <li>gpt-image: GPT-Image-1 (OpenAI API 직접 호출, 같은 API 키)</li>
 * </ul>
 */
@Slf4j
@Configuration
public class ImageProviderConfig {

    @Value("${image.provider:dalle}")
    private String provider;

    @Value("${spring.ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${image.gpt-image.quality:medium}")
    private String gptImageQuality;

    @Bean
    public ImageGenerationProvider imageGenerationProvider(ImageClient imageClient) {
        if ("gpt-image".equalsIgnoreCase(provider)) {
            log.info("Image provider: GPT-Image-1 (quality={}, OpenAI API key)", gptImageQuality);
            return new GptImageProvider(openaiApiKey, gptImageQuality);
        }

        log.info("Image provider: DALL-E 3 (Spring AI)");
        return new DalleImageProvider(imageClient);
    }
}
