package com.dreamtoon.domain.dream.service;

import com.dreamtoon.domain.dream.dto.TranscriptionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OpenAI Whisper 기반 음성 전사 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceTranscriptionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private static final String WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions";

    /**
     * 음성 파일을 텍스트로 전사
     *
     * @param audioFile 음성 파일 (MultipartFile)
     * @return 전사 결과
     */
    public TranscriptionResponse transcribeAudio(MultipartFile audioFile) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Starting audio transcription for file: {} (size: {} bytes)",
                    audioFile.getOriginalFilename(), audioFile.getSize());

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(openAiApiKey);

            // multipart 요청 body 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            });
            body.add("model", "whisper-1");
            body.add("language", "ko"); // 한국어 우선
            body.add("response_format", "json");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    WHISPER_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 응답 파싱
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String transcribedText = jsonResponse.get("text").asText();

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("Audio transcription completed in {}ms: {} characters",
                    processingTime, transcribedText.length());

            return TranscriptionResponse.builder()
                    .text(transcribedText)
                    .language("ko")
                    .processingTimeMs(processingTime)
                    .build();

        } catch (IOException e) {
            log.error("Failed to read audio file", e);
            throw new RuntimeException("음성 파일 읽기 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to transcribe audio", e);
            throw new RuntimeException("음성 전사 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 음성 파일 검증
     *
     * @param audioFile 검증할 파일
     * @throws IllegalArgumentException 유효하지 않은 파일인 경우
     */
    public void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("음성 파일이 비어있습니다");
        }

        // 파일 크기 제한 (25MB - Whisper API 제한)
        long maxSize = 25 * 1024 * 1024; // 25MB
        if (audioFile.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("파일 크기가 너무 큽니다 (최대 25MB). 현재: %.2fMB",
                            audioFile.getSize() / (1024.0 * 1024.0))
            );
        }

        // 지원하는 파일 형식 확인
        String contentType = audioFile.getContentType();
        if (contentType == null || !isSupportedAudioFormat(contentType)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파일 형식입니다. 지원 형식: mp3, mp4, mpeg, mpga, m4a, wav, webm"
            );
        }
    }

    /**
     * 지원되는 오디오 형식인지 확인
     */
    private boolean isSupportedAudioFormat(String contentType) {
        return contentType.contains("audio/") ||
                contentType.contains("video/mp4") ||
                contentType.contains("video/mpeg") ||
                contentType.contains("video/webm");
    }
}
