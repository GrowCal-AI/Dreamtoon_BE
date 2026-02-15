package com.dreamtoon.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Component;

/** OpenAI API 클라이언트 래퍼 Spring AI를 사용하여 GPT-4o 및 DALL-E 3와 통신 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final ChatClient chatClient;
    private final ImageClient imageClient;

    /**
     * GPT-4o로 텍스트 분석 (JSON 응답)
     *
     * @param prompt 분석 프롬프트
     * @return GPT-4o 응답 텍스트
     */
    public String analyzeWithGpt(String prompt) {
        try {
            log.info("Sending request to GPT-4o for dream analysis");

            Prompt chatPrompt = new Prompt(new UserMessage(prompt));
            ChatResponse response = chatClient.call(chatPrompt);

            String content = response.getResult().getOutput().getContent();
            log.info("Received response from GPT-4o: {} characters", content.length());

            return content;

        } catch (Exception e) {
            log.error("Error calling GPT-4o API", e);
            throw new RuntimeException("GPT-4o API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * DALL-E 3로 이미지 생성
     *
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 이미지 URL (임시 URL, 60분 유효)
     */
    public String generateImage(String prompt) {
        try {
            log.info("Sending request to DALL-E 3 for image generation");

            // DALL-E 3 옵션: 1024x1792 (웹툰 세로 비율)
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
            log.info("Received image URL from DALL-E 3: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("Error calling DALL-E 3 API", e);
            throw new RuntimeException("DALL-E 3 API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Whisper로 음성을 텍스트로 변환 TODO: Spring AI Audio 모듈 추가 후 구현
     *
     * @param audioData 오디오 데이터
     * @return 변환된 텍스트
     */
    public String transcribeAudio(byte[] audioData) {
        // Spring AI 0.8.1은 아직 Audio 모듈이 안정화되지 않음
        // Phase 2.4에서 별도 구현 또는 OpenAI REST API 직접 호출
        throw new UnsupportedOperationException("음성 전사 기능은 아직 구현되지 않았습니다");
    }
}
