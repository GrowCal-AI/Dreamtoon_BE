package com.dreamtoon.global.config;

import com.dreamtoon.global.logging.RequestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**")  // API 경로에만 적용
                .excludePathPatterns(
                        "/api-docs/**",      // Swagger 문서 제외
                        "/swagger-ui/**",    // Swagger UI 제외
                        "/actuator/**"       // Actuator 제외
                );
    }
}
