package com.dreamtoon.infrastructure.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.lang.reflect.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 로컬(local) 프로필용 Storage 빈.
 * ADC(Application Default Credentials)가 있으면 실제 GCS에 연결하고,
 * 없으면 no-op 프록시로 fallback합니다.
 *
 * ADC 설정: gcloud auth application-default login
 */
@Slf4j
@Configuration
@Profile("local")
public class LocalGcsConfig {

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    @Bean
    public Storage storage() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            Storage realStorage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();
            log.info("Using REAL GCS Storage for local profile (ADC found).");
            return realStorage;
        } catch (Exception e) {
            log.warn(
                    "No GCP credentials found. Using no-op Storage proxy. "
                            + "Run 'gcloud auth application-default login' to enable real GCS uploads.");
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
}
