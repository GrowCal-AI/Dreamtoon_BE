package com.dreamtoon.infrastructure.storage;

import com.google.cloud.storage.Storage;
import java.lang.reflect.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 로컬(local) 프로필에서 GCP credential 없이 기동하기 위해 No-op Storage 빈 제공. GCS 업로드/삭제 등 실제 스토리지 호출은 동작하지 않으며,
 * 실제 GCS 사용 시에는 GOOGLE_APPLICATION_CREDENTIALS 설정 후 application-local.yml 에서
 * spring.cloud.gcp.storage.enabled: true 로 변경하세요.
 */
@Slf4j
@Configuration
@Profile("local")
public class LocalGcsConfig {

    @Bean
    public Storage storage() {
        log.info("Using no-op GCP Storage proxy for local profile (no credentials required).");
        return (Storage)
                Proxy.newProxyInstance(
                        Storage.class.getClassLoader(),
                        new Class<?>[] {Storage.class},
                        (proxy, method, args) -> {
                            return switch (method.getName()) {
                                case "hashCode" -> System.identityHashCode(proxy);
                                case "equals" -> proxy == args[0];
                                case "toString" -> "NoOpStorage(local)";
                                default -> null;
                            };
                        });
    }
}
