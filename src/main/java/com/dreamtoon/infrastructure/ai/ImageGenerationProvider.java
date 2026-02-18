package com.dreamtoon.infrastructure.ai;

/** 이미지 생성 프로바이더 인터페이스. DALL-E, FLUX 등 다양한 이미지 생성 백엔드를 추상화. */
public interface ImageGenerationProvider {

    /**
     * 텍스트 프롬프트로 이미지 생성
     *
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 이미지 URL (또는 임시 URL)
     */
    String generateImage(String prompt);

    /** 현재 프로바이더 이름 (로깅용) */
    String getProviderName();
}
