package com.dreamtoon.infrastructure.storage;

import io.awspring.cloud.s3.S3Template;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** AWS S3 파일 저장소 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * 이미지 URL에서 다운로드하여 S3에 업로드
     *
     * @param imageUrl 임시 이미지 URL (DALL-E 생성 이미지)
     * @param prefix S3 파일 경로 prefix (예: "scenes", "webtoons")
     * @return S3에 저장된 영구 URL
     */
    public String uploadImageFromUrl(String imageUrl, String prefix) {
        try {
            log.info("Downloading image from URL: {}", imageUrl);

            // URL에서 이미지 다운로드
            byte[] imageData = downloadImageBytes(imageUrl);

            // S3에 업로드
            String s3Key = generateS3Key(prefix, "png");
            return uploadImage(imageData, s3Key, "image/png");

        } catch (Exception e) {
            log.error("Failed to upload image from URL to S3: {}", imageUrl, e);
            throw new RuntimeException("S3 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 바이트 배열 이미지를 S3에 업로드
     *
     * @param imageData 이미지 데이터
     * @param s3Key S3 객체 키
     * @param contentType 컨텐츠 타입
     * @return S3 URL
     */
    public String uploadImage(byte[] imageData, String s3Key, String contentType) {
        try {
            log.info("Uploading image to S3: bucket={}, key={}", bucketName, s3Key);

            try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
                s3Template.upload(bucketName, s3Key, inputStream);
            }

            // S3 URL 생성
            String s3Url =
                    String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);

            log.info("Image uploaded successfully to S3: {}", s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Failed to upload image to S3", e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * S3에서 이미지 삭제
     *
     * @param s3Url S3 URL
     */
    public void deleteImage(String s3Url) {
        try {
            // URL에서 S3 키 추출
            String s3Key = extractS3KeyFromUrl(s3Url);

            log.info("Deleting image from S3: bucket={}, key={}", bucketName, s3Key);
            s3Template.deleteObject(bucketName, s3Key);

            log.info("Image deleted successfully from S3: {}", s3Key);

        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", s3Url, e);
            // 삭제 실패는 치명적이지 않으므로 예외를 throw하지 않음
        }
    }

    /** URL에서 이미지 다운로드 */
    private byte[] downloadImageBytes(String imageUrl) throws IOException {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /** S3 키 생성 (고유한 파일명) */
    private String generateS3Key(String prefix, String extension) {
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s/%s_%s.%s", prefix, timestamp, uuid, extension);
    }

    /** S3 URL에서 키 추출 */
    private String extractS3KeyFromUrl(String s3Url) {
        // https://bucket-name.s3.region.amazonaws.com/prefix/file.png
        // -> prefix/file.png
        String[] parts = s3Url.split(".amazonaws.com/");
        if (parts.length == 2) {
            return parts[1];
        }
        throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
    }
}
