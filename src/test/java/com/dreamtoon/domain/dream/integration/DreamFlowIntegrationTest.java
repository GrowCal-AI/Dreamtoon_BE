package com.dreamtoon.domain.dream.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dreamtoon.domain.dream.dto.*;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.entity.ProcessingStatus;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import com.dreamtoon.domain.user.entity.Role;
import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import com.dreamtoon.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Blueprint v2.0 전체 플로우 통합 테스트
 * 
 * 테스트 시나리오:
 * 1. 꿈 기록 시작 (POST /api/v1/dreams)
 * 2. 감정 선택 (PATCH /api/v1/dreams/{id}/emotion)
 * 3. 상세 설명 입력 (PATCH /api/v1/dreams/{id}/details)
 * 4. 분석 결과 조회 (GET /api/v1/dreams/{id}/analysis)
 * 5. 웹툰 생성 (POST /api/v1/dreams/{id}/webtoon)
 * 6. 최종 결과 확인 (GET /api/v1/dreams/{id})
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DreamFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DreamRepository dreamRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@dreamtoon.com")
                .nickname("테스트유저")
                .socialProvider(SocialProvider.KAKAO)
                .socialId("test-social-id")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("전체 꿈 생성 플로우 테스트 (E2E)")
    @WithMockUser(username = "1") // testUser.id = 1
    void testCompleteDreamFlow() throws Exception {
        // Step 1: 꿈 기록 시작
        InitiateDreamRequest initiateRequest = InitiateDreamRequest.builder()
                .dreamContent("어젯밤 꿈에서 하늘을 날고 있었어요. 구름 위를 자유롭게 날아다니는데 너무 행복했습니다.")
                .build();

        MvcResult initiateResult = mockMvc.perform(post("/api/v1/dreams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initiateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dreamId").exists())
                .andExpect(jsonPath("$.data.systemMessage").exists())
                .andReturn();

        String initiateResponse = initiateResult.getResponse().getContentAsString();
        Long dreamId = objectMapper.readTree(initiateResponse)
                .get("data").get("dreamId").asLong();

        System.out.println("✅ Step 1: 꿈 기록 시작 완료 (dreamId: " + dreamId + ")");

        // Step 2: 감정 선택
        EmotionSelectRequest emotionRequest = EmotionSelectRequest.builder()
                .primaryEmotion(EmotionType.JOY)
                .build();

        mockMvc.perform(patch("/api/v1/dreams/" + dreamId + "/emotion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emotionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.systemMessage").value("좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?"));

        System.out.println("✅ Step 2: 감정 선택 완료 (JOY)");

        // Step 3: 상세 설명 입력 (AI 분석 시작)
        DreamDetailsRequest detailsRequest = DreamDetailsRequest.builder()
                .detailedDescription("구름 위를 날면서 아래를 내려다보니 제가 사는 동네가 보였어요.")
                .realLifeContext("요즘 회사 업무가 많아서 스트레스를 많이 받고 있어요.")
                .build();

        mockMvc.perform(patch("/api/v1/dreams/" + dreamId + "/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detailsRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("ANALYZING"));

        System.out.println("✅ Step 3: 상세 설명 입력 완료 (AI 분석 시작)");

        // Step 4: 분석 결과 조회 (폴링 시뮬레이션)
        // 실제로는 비동기 처리가 완료될 때까지 대기
        Thread.sleep(2000); // 2초 대기 (실제 환경에서는 더 길 수 있음)

        mockMvc.perform(get("/api/v1/dreams/" + dreamId + "/analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dreamId").value(dreamId))
                .andExpect(jsonPath("$.data.status").exists());

        System.out.println("✅ Step 4: 분석 결과 조회 완료");

        // Step 5: 웹툰 생성 (장르 선택)
        WebtoonGenerateRequest webtoonRequest = WebtoonGenerateRequest.builder()
                .selectedGenre(Genre.FANTASY)
                .build();

        mockMvc.perform(post("/api/v1/dreams/" + dreamId + "/webtoon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webtoonRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("GENERATING"));

        System.out.println("✅ Step 5: 웹툰 생성 시작 (FANTASY)");

        // Step 6: 최종 결과 확인
        Thread.sleep(2000); // 2초 대기

        mockMvc.perform(get("/api/v1/dreams/" + dreamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dreamId").value(dreamId))
                .andExpect(jsonPath("$.data.dreamContent").exists())
                .andExpect(jsonPath("$.data.primaryEmotion").value("JOY"))
                .andExpect(jsonPath("$.data.selectedGenre").value("FANTASY"));

        System.out.println("✅ Step 6: 최종 결과 확인 완료");
        System.out.println("\n🎉 전체 플로우 테스트 성공!");
    }

    @Test
    @DisplayName("라이브러리 기능 테스트")
    @WithMockUser(username = "1")
    void testLibraryFeatures() throws Exception {
        // 꿈 생성
        InitiateDreamRequest request = InitiateDreamRequest.builder()
                .dreamContent("테스트 꿈 내용")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/dreams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long dreamId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("dreamId").asLong();

        // 라이브러리에 추가
        mockMvc.perform(post("/api/v1/dreams/" + dreamId + "/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isInLibrary").value(true));

        System.out.println("✅ 라이브러리 추가 완료");

        // 즐겨찾기 토글
        mockMvc.perform(patch("/api/v1/dreams/" + dreamId + "/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorite").value(true));

        System.out.println("✅ 즐겨찾기 토글 완료");

        // 라이브러리 조회
        mockMvc.perform(get("/api/v1/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dreams").isArray());

        System.out.println("✅ 라이브러리 조회 완료");
    }

    @Test
    @DisplayName("잘못된 순서로 API 호출 시 에러 테스트")
    @WithMockUser(username = "1")
    void testInvalidFlowOrder() throws Exception {
        // 꿈 생성
        InitiateDreamRequest request = InitiateDreamRequest.builder()
                .dreamContent("테스트 꿈 내용")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/dreams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long dreamId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("dreamId").asLong();

        // 감정 선택 없이 바로 웹툰 생성 시도 (실패해야 함)
        WebtoonGenerateRequest webtoonRequest = WebtoonGenerateRequest.builder()
                .selectedGenre(Genre.FANTASY)
                .build();

        mockMvc.perform(post("/api/v1/dreams/" + dreamId + "/webtoon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webtoonRequest)))
                .andExpect(status().isBadRequest());

        System.out.println("✅ 잘못된 순서 호출 에러 처리 확인");
    }
}
