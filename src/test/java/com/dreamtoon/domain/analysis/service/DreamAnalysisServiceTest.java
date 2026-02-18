package com.dreamtoon.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.dreamtoon.domain.analysis.dto.DreamAnalysisResponse;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DreamAnalysisServiceTest {

    @Mock private DreamRepository dreamRepository;

    @InjectMocks private DreamAnalysisService dreamAnalysisService;

    @Test
    @DisplayName("데이터가 없을 때 empty state 반환")
    void getDashboardAnalysis_empty() {
        // given
        Long userId = 1L;
        given(dreamRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .willReturn(new ArrayList<>());

        // when
        DreamAnalysisResponse response = dreamAnalysisService.getDashboardAnalysis(userId, 7);

        // then
        assertThat(response.isHasEnoughData()).isFalse();
        assertThat(response.getMessage()).contains("아직 분석할 꿈 데이터가 없어요");
        assertThat(response.getStressIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("데이터가 있을 때 정상 분석 결과 반환")
    void getDashboardAnalysis_success() {
        // given
        Long userId = 1L;
        List<Dream> dreams = new ArrayList<>();

        // Mock Dream 1: High Anxiety
        Dream dream1 = Dream.builder().user(null).dreamContent("test").build();
        Map<String, Integer> scores1 = new HashMap<>();
        scores1.put(EmotionType.ANXIETY.getCode(), 80);
        scores1.put(EmotionType.JOY.getCode(), 10);
        ReflectionTestUtils.setField(dream1, "emotionScores", scores1);
        ReflectionTestUtils.setField(dream1, "createdAt", LocalDateTime.now().minusDays(1));
        dreams.add(dream1);

        // Mock Dream 2: High Joy
        Dream dream2 = Dream.builder().user(null).dreamContent("test").build();
        Map<String, Integer> scores2 = new HashMap<>();
        scores2.put(EmotionType.ANXIETY.getCode(), 10);
        scores2.put(EmotionType.JOY.getCode(), 90);
        ReflectionTestUtils.setField(dream2, "emotionScores", scores2);
        ReflectionTestUtils.setField(dream2, "createdAt", LocalDateTime.now().minusDays(2));
        dreams.add(dream2);

        given(dreamRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .willReturn(dreams);

        // when
        DreamAnalysisResponse response = dreamAnalysisService.getDashboardAnalysis(userId, 7);

        // then
        assertThat(response.isHasEnoughData()).isTrue();

        // Stress Calculation:
        // Dream 1: Anxiety 80 = Stress 80
        // Dream 2: Anxiety 10 = Stress 10
        // Avg Stress = (80 + 10) / 2 = 45
        assertThat(response.getStressIndex()).isEqualTo(45);
        assertThat(response.getStressLevel()).isEqualTo("MEDIUM"); // 40~69

        // Sleep Quality: 80 - (45/2) = 80 - 22 = 58
        assertThat(response.getSleepQualityScore()).isEqualTo(58);

        // Emotion Balance
        // Joy: (10 + 90) / 2 = 50
        // Anxiety: (80 + 10) / 2 = 45
        assertThat(response.getEmotionBalance().getJoy()).isEqualTo(50);
        assertThat(response.getEmotionBalance().getAnxiety()).isEqualTo(45);
    }
}
