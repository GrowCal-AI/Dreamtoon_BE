package com.dreamtoon.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * GPT-Image-1 이미지 생성 프로바이더 (OpenAI API 직접 호출).
 * Spring AI 0.8.1이 gpt-image-1을 지원하지 않으므로 직접 REST 호출.
 * 같은 OPENAI_API_KEY로 동작하며, DALL-E 3보다 빠르고 프롬프트 충실도가 높음.
 */
@Slf4j
public class GptImageProvider implements ImageGenerationProvider {

    private static final String OPENAI_IMAGES_URL = "https://api.openai.com/v1/images/generations";

    private final String apiKey;
    private final String quality; // "low", "medium", "high"
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GptImageProvider(String apiKey, String quality) {
        this.apiKey = apiKey;
        this.quality = quality != null ? quality : "medium";
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateImage(String prompt) {
        try {
            log.info("[GPT-Image-1] Generating image (quality={})...", quality);

            Map<String, Object> body =
                    Map.of(
                            "model", "gpt-image-1",
                            "prompt", prompt,
                            "n", 1,
                            "size", "1024x1536",
                            "quality", quality);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(OPENAI_IMAGES_URL))
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .timeout(Duration.ofSeconds(120))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error(
                        "[GPT-Image-1] API error: status={}, body={}",
                        response.statusCode(),
                        response.body());
                throw new RuntimeException(
                        "GPT-Image-1 API 오류: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode dataNode = root.path("data").get(0);

            // gpt-image-1은 기본적으로 b64_json 반환, url이 있으면 url 사용
            if (dataNode.has("url") && !dataNode.get("url").asText().isEmpty()) {
                String imageUrl = dataNode.get("url").asText();
                log.info("[GPT-Image-1] Image generated (URL): {}", imageUrl);
                return imageUrl;
            } else if (dataNode.has("b64_json")) {
                // base64 이미지를 data URI로 변환 (GCS 업로드 시 별도 처리 필요)
                String base64 = dataNode.get("b64_json").asText();
                log.info("[GPT-Image-1] Image generated (base64, {} chars)", base64.length());
                return "data:image/png;base64," + base64;
            }

            throw new RuntimeException("GPT-Image-1 응답에 이미지 데이터 없음");

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GPT-Image-1] Failed to generate image", e);
            throw new RuntimeException("GPT-Image-1 이미지 생성 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "GPT-Image-1 (" + quality + ")";
    }
}
