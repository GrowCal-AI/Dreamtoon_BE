package com.dreamtoon.support;

import static org.mockito.Mockito.mock;

import com.google.cloud.storage.Storage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/** 테스트 환경에서 GCP Storage Bean을 Mock으로 대체하여 credential 로드를 방지 */
@Configuration
@Profile("test")
public class TestGcsConfig {

    @Bean
    @Primary
    public Storage storage() {
        return mock(Storage.class);
    }
}
