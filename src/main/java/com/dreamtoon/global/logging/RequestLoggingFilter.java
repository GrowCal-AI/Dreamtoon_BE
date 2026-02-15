package com.dreamtoon.global.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * HTTP 요청 로깅 필터
 * - IP 주소, 엔드포인트, HTTP 메서드, 요청 Body, 인증 토큰 로깅
 * - 응답은 로깅하지 않음 (명시적 요구사항)
 */
@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";
    private static final String CLIENT_IP = "clientIp";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // ContentCachingRequestWrapper로 래핑 (body를 여러 번 읽을 수 있도록)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);

        // MDC에 요청 ID 및 클라이언트 IP 설정
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String clientIp = getClientIp(httpRequest);

        MDC.put(REQUEST_ID, requestId);
        MDC.put(CLIENT_IP, clientIp);

        try {
            long startTime = System.currentTimeMillis();

            // 요청 로깅
            logRequest(wrappedRequest, requestId, clientIp);

            // 다음 필터 체인 실행
            chain.doFilter(wrappedRequest, response);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Request completed in {}ms", duration);

        } finally {
            // MDC 정리
            MDC.clear();
        }
    }

    /**
     * 요청 정보 로깅
     */
    private void logRequest(ContentCachingRequestWrapper request, String requestId, String clientIp) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String token = extractToken(request);

        StringBuilder logMessage = new StringBuilder("\n");
        logMessage.append("========== HTTP Request ==========\n");
        logMessage.append("Request ID    : ").append(requestId).append("\n");
        logMessage.append("Client IP     : ").append(clientIp).append("\n");
        logMessage.append("HTTP Method   : ").append(method).append("\n");
        logMessage.append("Endpoint      : ").append(uri);

        if (queryString != null) {
            logMessage.append("?").append(queryString);
        }
        logMessage.append("\n");

        if (token != null) {
            logMessage.append("Auth Token    : ").append(maskToken(token)).append("\n");
        }

        // Request Body 로깅 (GET 요청은 제외)
        if (!"GET".equals(method) && !"DELETE".equals(method)) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                logMessage.append("Request Body  :\n").append(body).append("\n");
            }
        }

        logMessage.append("==================================");

        log.info(logMessage.toString());
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 여러 프록시를 거친 경우 첫 번째 IP 반환
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * Authorization 헤더에서 토큰 추출
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 토큰 마스킹 (보안을 위해 일부만 표시)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }

    /**
     * Request Body 내용 추출
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);

                // JSON body인 경우 비밀번호 필드 마스킹
                if (body.contains("password")) {
                    body = body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
                }

                return body;
            }
        } catch (Exception e) {
            log.warn("Failed to read request body", e);
        }
        return null;
    }
}
