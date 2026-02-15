package com.dreamtoon.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 컨트롤러 레벨 요청 인터셉터
 * - 어떤 컨트롤러 메서드가 호출되는지 로깅
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();

            String requestId = MDC.get("requestId");
            log.info("[{}] Controller: {}.{}()", requestId, controllerName, methodName);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String requestId = MDC.get("requestId");
        int status = response.getStatus();

        if (ex != null) {
            log.error("[{}] Request failed with exception: {}", requestId, ex.getMessage());
        } else if (status >= 400) {
            log.warn("[{}] Request completed with error status: {}", requestId, status);
        } else {
            log.debug("[{}] Request completed successfully with status: {}", requestId, status);
        }
    }
}
