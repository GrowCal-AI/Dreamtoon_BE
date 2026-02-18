package com.dreamtoon.domain.dream.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dreamtoon.domain.dream.dto.DreamDetailsRequest;
import com.dreamtoon.domain.dream.dto.EmotionSelectRequest;
import com.dreamtoon.domain.dream.dto.InitiateDreamRequest;
import com.dreamtoon.domain.dream.dto.WebtoonGenerateRequest;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.user.entity.Role;
import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.dreamtoon.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Blueprint v2.0 전체 플로우 통합 테스트
 *
 * <p>테스트 시나리오: 1. 꿈 기록 시작 (POST /api/v1/dreams) 2. 감정 선택 (PATCH /api/v1/dreams/{id}/emotion) 3. 상세
 * 설명 입력 (PATCH /api/v1/dreams/{id}/details) 4. 꿈 조회 (GET /api/v1/dreams/{id}) 5. 라이브러리/즐겨찾기 기능
 *
 * <p>인증: JwtTokenProvider로 실제 저장된 userId 기반 Bearer 토큰 생성 (H2 auto_increment ID가 매 테스트마다 달라지므로, 동적
 * 토큰 사용)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DreamFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private DreamRepository dreamRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String bearerToken;

    @BeforeEach
    void setUp() {
        testUser =
                User.builder()
                        .email("test@dreamtoon.com")
                        .nickname("테스트유저")
                        .socialProvider(SocialProvider.KAKAO)
                        .socialId("test-social-id")
                        .role(Role.ROLE_USER)
                        .build();
        userRepository.save(testUser);

        // 실제 저장된 userId로 JWT 토큰 생성 (H2 sequence ID가 매 테스트마다 다르므로 동적으로 생성)
        bearerToken =
                "Bearer "
                        + jwtTokenProvider.createAccessToken(
                                testUser.getId(), testUser.getEmail(), testUser.getRole().name());
    }

    @Test
    @DisplayName("전체 꿈 생성 플로우 테스트 (E2E)")
    void testCompleteDreamFlow() throws Exception {
        // Step 1: 꿈 기록 시작
        InitiateDreamRequest initiateRequest =
                InitiateDreamRequest.builder()
                        .dreamContent("어젯밤 꿈에서 하늘을 날고 있었어요. 구름 위를 자유롭게 날아다니는데 너무 행복했습니다.")
                        .build();

        MvcResult initiateResult =
                mockMvc.perform(
                                post("/api/v1/dreams")
                                        .header("Authorization", bearerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(initiateRequest)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.dreamId").exists())
                        .andExpect(jsonPath("$.data.systemMessage").exists())
                        .andReturn();

        String initiateResponse = initiateResult.getResponse().getContentAsString();
        Long dreamId = objectMapper.readTree(initiateResponse).get("data").get("dreamId").asLong();

        System.out.println("✅ Step 1: 꿈 기록 시작 완료 (dreamId: " + dreamId + ")");

        // Step 2: 감정 선택
        EmotionSelectRequest emotionRequest =
                EmotionSelectRequest.builder().primaryEmotion(EmotionType.JOY).build();

        mockMvc.perform(
                        patch("/api/v1/dreams/" + dreamId + "/emotion")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(emotionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.systemMessage").value("좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?"));

        System.out.println("✅ Step 2: 감정 선택 완료 (JOY)");

        // Step 3: 상세 설명 입력 (비동기 AI 분석 시작)
        DreamDetailsRequest detailsRequest =
                DreamDetailsRequest.builder()
                        .detailedDescription("구름 위를 날면서 아래를 내려다보니 제가 사는 동네가 보였어요.")
                        .realLifeContext("요즘 회사 업무가 많아서 스트레스를 많이 받고 있어요.")
                        .build();

        mockMvc.perform(
                        patch("/api/v1/dreams/" + dreamId + "/details")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(detailsRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("ANALYZING"));

        System.out.println("✅ Step 3: 상세 설명 입력 완료 (AI 분석 시작)");

        // Step 4: 꿈 조회 (비동기 처리 결과와 무관하게 dreamId 확인)
        mockMvc.perform(get("/api/v1/dreams/" + dreamId).header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(String.valueOf(dreamId)))
                .andExpect(jsonPath("$.data.content").exists());

        System.out.println("✅ Step 4: 꿈 조회 완료");
        System.out.println("\n🎉 핵심 플로우 테스트 성공!");
    }

    @Test
    @DisplayName("라이브러리 기능 테스트")
    void testLibraryFeatures() throws Exception {
        // 꿈 생성
        InitiateDreamRequest request =
                InitiateDreamRequest.builder().dreamContent("테스트 꿈 내용").build();

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/dreams")
                                        .header("Authorization", bearerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andReturn();

        Long dreamId =
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .get("data")
                        .get("dreamId")
                        .asLong();

        // 라이브러리에 추가
        mockMvc.perform(
                        post("/api/v1/dreams/" + dreamId + "/library")
                                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isInLibrary").value(true));

        System.out.println("✅ 라이브러리 추가 완료");

        // 즐겨찾기 토글
        mockMvc.perform(
                        patch("/api/v1/dreams/" + dreamId + "/favorite")
                                .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorite").value(true));

        System.out.println("✅ 즐겨찾기 토글 완료");

        // 라이브러리 조회
        mockMvc.perform(get("/api/v1/library").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dreams").isArray());

        System.out.println("✅ 라이브러리 조회 완료");
    }

    @Test
    @DisplayName("잘못된 순서로 API 호출 시 에러 테스트")
    void testInvalidFlowOrder() throws Exception {
        // 꿈 생성
        InitiateDreamRequest request =
                InitiateDreamRequest.builder().dreamContent("테스트 꿈 내용").build();

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/dreams")
                                        .header("Authorization", bearerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andReturn();

        Long dreamId =
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .get("data")
                        .get("dreamId")
                        .asLong();

        // 분석 완료(ANALYSIS_COMPLETED) 전에 웹툰 생성 시도 → DREAM_INVALID_STATE(400)
        WebtoonGenerateRequest webtoonRequest =
                WebtoonGenerateRequest.builder().selectedGenre(Genre.DARK_FANTASY).build();

        mockMvc.perform(
                        post("/api/v1/dreams/" + dreamId + "/webtoon")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(webtoonRequest)))
                .andExpect(status().isBadRequest());

        System.out.println("✅ 잘못된 순서 호출 에러 처리 확인");
    }

    @Test
    @DisplayName("JWT 토큰 인증으로 꿈 생성 성공")
    void testAuthenticatedApiWithJwtToken() throws Exception {
        InitiateDreamRequest request =
                InitiateDreamRequest.builder().dreamContent("JWT 토큰 테스트용 꿈").build();

        mockMvc.perform(
                        post("/api/v1/dreams")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dreamId").exists());
    }
}
