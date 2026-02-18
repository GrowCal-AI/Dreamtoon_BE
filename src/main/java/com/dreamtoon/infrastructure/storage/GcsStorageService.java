package com.dreamtoon.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Google Cloud Storage 파일 저장소 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GcsStorageService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    /**
     * 이미지 URL에서 다운로드하여 GCS에 업로드
     *
     * @param imageUrl 임시 이미지 URL (DALL-E 생성 이미지)
     * @param prefix GCS 파일 경로 prefix (예: "webtoon/dream_5")
     * @return GCS에 저장된 영구 URL
     */
    public String uploadImageFromUrl(String imageUrl, String prefix) {
        try {
            byte[] imageData;
            String contentType = "image/png";

            if (imageUrl.startsWith("data:")) {
                // base64 data URI 처리 (GPT-Image-1 등)
                log.info("Decoding base64 image data for GCS upload");
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                imageData = Base64.getDecoder().decode(base64Data);
                if (imageUrl.contains("image/jpeg")) {
                    contentType = "image/jpeg";
                }
            } else {
                log.info("Downloading image from URL: {}", imageUrl);
                imageData = downloadImageBytes(imageUrl);
            }

            String extension = contentType.endsWith("jpeg") ? "jpg" : "png";
            String gcsKey = generateGcsKey(prefix, extension);
            return uploadImage(imageData, gcsKey, contentType);

        } catch (Exception e) {
            log.error("Failed to upload image to GCS: {}", imageUrl.substring(0, Math.min(100, imageUrl.length())), e);
            throw new RuntimeException("GCS 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 바이트 배열 이미지를 GCS에 업로드
     *
     * @param imageData 이미지 데이터
     * @param gcsKey GCS 객체 키
     * @param contentType 컨텐츠 타입
     * @return GCS 공개 URL
     */
    public String uploadImage(byte[] imageData, String gcsKey, String contentType) {
        try {
            log.info("Uploading image to GCS: bucket={}, key={}", bucketName, gcsKey);

            BlobId blobId = BlobId.of(bucketName, gcsKey);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
            storage.create(blobInfo, imageData);

            String gcsUrl =
                    String.format("https://storage.googleapis.com/%s/%s", bucketName, gcsKey);

            log.info("Image uploaded successfully to GCS: {}", gcsUrl);
            return gcsUrl;

        } catch (Exception e) {
            log.error("Failed to upload image to GCS", e);
            throw new RuntimeException("GCS 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * GCS에서 이미지 삭제
     *
     * @param gcsUrl GCS 공개 URL
     */
    public void deleteImage(String gcsUrl) {
        try {
            String gcsKey = extractGcsKeyFromUrl(gcsUrl);

            log.info("Deleting image from GCS: bucket={}, key={}", bucketName, gcsKey);
            storage.delete(BlobId.of(bucketName, gcsKey));

            log.info("Image deleted successfully from GCS: {}", gcsKey);

        } catch (Exception e) {
            log.error("Failed to delete image from GCS: {}", gcsUrl, e);
            // 삭제 실패는 치명적이지 않으므로 예외를 throw하지 않음
        }
    }

    /** URL에서 이미지 다운로드 */
    private byte[] downloadImageBytes(String imageUrl) throws IOException {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /** GCS 키 생성 (고유한 파일명) */
    private String generateGcsKey(String prefix, String extension) {
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s/%s_%s.%s", prefix, timestamp, uuid, extension);
    }

    /** GCS URL에서 객체 키 추출 */
    private String extractGcsKeyFromUrl(String gcsUrl) {
        // https://storage.googleapis.com/bucket-name/prefix/file.png
        // -> prefix/file.png
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (gcsUrl.startsWith(prefix)) {
            return gcsUrl.substring(prefix.length());
        }
        throw new IllegalArgumentException("Invalid GCS URL format: " + gcsUrl);
    }
}
