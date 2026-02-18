package com.dreamtoon.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        Components components =
                new Components()
                        .addSecuritySchemes(
                                jwt,
                                new SecurityScheme()
                                        .name(jwt)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(
                        new Info()
                                .title("DreamToon API")
                                .description(
                                        "**DreamToon** — AI 기반 꿈 시각화·정서 분석 헬스케어 플랫폼의 백엔드"
                                            + " API입니다.\n\n"
                                            + "**🔐 인증**: 대부분의 API는 **Authorization: Bearer"
                                            + " {accessToken}** 헤더가 필요합니다. 로그인은"
                                            + " OAuth2(Google/Kakao)로 진행되며, 토큰은 **Auth** 태그의"
                                            + " `/auth/refresh`로 갱신할 수 있습니다. 개발 시에는 **Auth > [DEV]"
                                            + " 테스트 로그인**으로 토큰 발급 후 상단 **Authorize**에 입력하세요.\n\n"
                                            + "**📌 주요 플로우**: **Dreams** — 꿈 기록 시작 → 감정 선택 → 상세 입력"
                                            + " → 분석 조회(폴링) → 4컷 웹툰 생성. **Library** — 저장한 꿈 목록 조회."
                                            + " **Users** — 내 정보/닉네임/권한. **Subscriptions** — 구독/사용량"
                                            + " 관리.")
                                .version("v1.0.0"))
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:" + serverPort)
                                        .description("Local Server"),
                                new Server()
                                        .url("https://api.dreamtoon.com")
                                        .description("Production Server")))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
