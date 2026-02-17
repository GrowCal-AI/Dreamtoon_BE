package com.dreamtoon.global.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
public class AwsS3Config {

    @Value("${AWS_ACCESS_KEY_ID:test}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:test}")
    private String secretAccessKey;

    @Value("${AWS_REGION:ap-northeast-2}")
    private String region;

    @Value("${AWS_S3_ENDPOINT:}")
    private String s3Endpoint;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials =
                AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        var s3ClientBuilder =
                S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

        // LocalStack 엔드포인트 설정 (로컬 개발용)
        if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
            log.info("Using custom S3 endpoint: {}", s3Endpoint);
            s3ClientBuilder
                    .endpointOverride(URI.create(s3Endpoint))
                    .forcePathStyle(true); // LocalStack은 path-style 필요
        }

        S3Client client = s3ClientBuilder.build();
        log.info("S3Client initialized with region: {}", region);
        return client;
    }
}
