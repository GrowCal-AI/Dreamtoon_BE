package com.dreamtoon.domain.dream.controller;

import com.dreamtoon.global.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Voice", description = "음성 → 텍스트 변환 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceController {

    @Operation(
            summary = "음성 파일을 텍스트로 변환",
            description = "음성 파일(audio)을 업로드하면 Whisper API로 텍스트를 추출합니다.")
    @PostMapping("/transcribe")
    public ResponseEntity<ApiResponse<Map<String, String>>> transcribe(
            @RequestParam("audio") MultipartFile audioFile) {
        // TODO: OpenAI Whisper API 연동 구현
        log.info("Voice transcription requested, file size: {} bytes", audioFile.getSize());
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("text", "음성 전사 기능은 곧 구현됩니다. (파일 수신 완료)")));
    }
}
