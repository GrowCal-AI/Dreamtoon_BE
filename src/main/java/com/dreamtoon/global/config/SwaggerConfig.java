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
                                .description("AI 기반 꿈 시각화 및 정서 상태 분석 헬스케어 플랫폼 API")
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
