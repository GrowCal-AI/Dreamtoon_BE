package com.dreamtoon.global.config;

import com.dreamtoon.global.logging.RequestLoggingFilter;
import com.dreamtoon.infrastructure.security.CustomOAuth2UserService;
import com.dreamtoon.infrastructure.security.JwtAuthenticationFilter;
import com.dreamtoon.infrastructure.security.OAuth2AuthenticationSuccessHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RequestLoggingFilter requestLoggingFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @org.springframework.beans.factory.annotation.Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // Public endpoints
                                        .requestMatchers("/", "/error", "/favicon.ico")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                        .permitAll()

                                        // Test login endpoint (dev only)
                                        .requestMatchers("/api/v1/auth/test-login")
                                        .permitAll()

                                        // OAuth2 login (Spring Security 기본 경로)
                                        .requestMatchers("/oauth2/**", "/login/oauth2/**")
                                        .permitAll()

                                        // Auth API (토큰 갱신)
                                        .requestMatchers(
                                                "/api/v1/auth/refresh", "/api/v1/auth/logout")
                                        .permitAll()

                                        // Polar.sh Webhook (서명으로 자체 검증)
                                        .requestMatchers("/api/v1/webhooks/polar")
                                        .permitAll()

                                        // 비회원 체험 API
                                        .requestMatchers("/api/v1/guest/**")
                                        .permitAll()

                                        // Authenticated endpoints
                                        .requestMatchers("/api/v1/dreams/**")
                                        .authenticated()
                                        .requestMatchers("/api/v1/users/**")
                                        .authenticated()
                                        .requestMatchers("/api/v1/analytics/**")
                                        .authenticated()
                                        .requestMatchers("/api/v1/voice/**")
                                        .authenticated()
                                        .requestMatchers("/api/v1/library/**")
                                        .authenticated()
                                        .requestMatchers("/api/v1/subscriptions/**")
                                        .authenticated()
                                        .anyRequest()
                                        .authenticated())
                .oauth2Login(
                        oauth2 ->
                                oauth2.userInfoEndpoint(
                                                userInfo ->
                                                        userInfo.userService(
                                                                customOAuth2UserService))
                                        .successHandler(oAuth2AuthenticationSuccessHandler))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
