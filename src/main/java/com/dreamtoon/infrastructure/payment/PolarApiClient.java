package com.dreamtoon.infrastructure.payment;

import com.dreamtoon.global.config.PolarProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Polar.sh REST API 클라이언트 (공식 Java SDK 없음 → HttpClient 직접 사용). */
@Slf4j
@Component
@RequiredArgsConstructor
public class PolarApiClient {

    private final PolarProperties polarProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 결제 체크아웃 세션 생성.
     *
     * @param productPriceId Polar.sh Price ID
     * @param customerEmail 결제할 사용자 이메일 (사전 입력용)
     * @param internalUserId 내부 user_id (Polar metadata에 저장)
     * @return 결제 URL 등이 포함된 응답 JSON
     */
    public JsonNode createCheckout(String productPriceId, String customerEmail, Long internalUserId)
            throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("product_price_id", productPriceId);
        body.put("success_url", polarProperties.getSuccessUrl());
        if (customerEmail != null && !customerEmail.isBlank()) {
            body.put("customer_email", customerEmail);
        }
        // 내부 user_id를 메타데이터로 전달 (Webhook에서 사용자 매핑에 활용 가능)
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("user_id", String.valueOf(internalUserId));
        body.set("metadata", metadata);

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(polarProperties.getBaseUrl() + "/checkouts/"))
                        .header("Authorization", "Bearer " + polarProperties.getApiKey())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            log.error(
                    "[Polar] Checkout creation failed: status={}, body={}",
                    response.statusCode(),
                    response.body());
            throw new RuntimeException(
                    "Polar checkout 생성 실패 [" + response.statusCode() + "]: " + response.body());
        }

        return objectMapper.readTree(response.body());
    }

    /**
     * Customer Portal URL 조회.
     *
     * @param polarCustomerId Polar.sh Customer ID
     * @return portal 세션 JSON (url 포함)
     */
    public JsonNode createCustomerPortalSession(String polarCustomerId) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("customer_id", polarCustomerId);

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(polarProperties.getBaseUrl() + "/customer-portal/sessions"))
                        .header("Authorization", "Bearer " + polarProperties.getApiKey())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            log.error(
                    "[Polar] Customer portal session failed: status={}, body={}",
                    response.statusCode(),
                    response.body());
            throw new RuntimeException(
                    "Polar portal 세션 생성 실패 [" + response.statusCode() + "]: " + response.body());
        }

        return objectMapper.readTree(response.body());
    }
}
