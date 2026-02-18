package com.dreamtoon.domain.analytics.service;

import com.dreamtoon.domain.analytics.dto.EmotionAnalysisResponse;
import com.dreamtoon.domain.analytics.dto.HealthIndexResponse;
import com.dreamtoon.domain.analytics.dto.PatternAnalysisResponse;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DreamRepository dreamRepository;

    public HealthIndexResponse getHealthIndex(Long userId) {
        List<Dream> dreams =
                dreamRepository.findByUserId(userId, PageRequest.of(0, 100)).getContent();

        if (dreams.isEmpty()) {
            return HealthIndexResponse.builder()
                    .stressLevel(0)
                    .anxietyLevel(0)
                    .emotionalResilience(50)
                    .relationshipStress(0)
                    .sleepQuality(50)
                    .nightmareRatio(0.0)
                    .insights(List.of("아직 기록된 꿈이 없습니다. 꿈을 기록해보세요!"))
                    .emotionDistribution(Collections.emptyMap())
                    .build();
        }

        Map<String, Integer> emotionTotals = new HashMap<>();
        int count = 0;
        for (Dream dream : dreams) {
            if (dream.getEmotionScores() != null) {
                dream.getEmotionScores().forEach((k, v) -> emotionTotals.merge(k, v, Integer::sum));
                count++;
            }
        }

        int finalCount = Math.max(count, 1);
        Map<String, Integer> avgEmotions = new HashMap<>();
        emotionTotals.forEach((k, v) -> avgEmotions.put(k, v / finalCount));

        int anxiety = avgEmotions.getOrDefault("ANXIETY", 0);
        int stress = (anxiety + avgEmotions.getOrDefault("ANGER", 0)) / 2;

        return HealthIndexResponse.builder()
                .stressLevel(stress)
                .anxietyLevel(anxiety)
                .emotionalResilience(Math.max(0, 100 - stress))
                .relationshipStress(avgEmotions.getOrDefault("SADNESS", 0))
                .sleepQuality(Math.max(0, 100 - anxiety))
                .nightmareRatio(0.0)
                .insights(List.of("꿈 데이터를 기반으로 분석되었습니다."))
                .emotionDistribution(avgEmotions)
                .build();
    }

    public EmotionAnalysisResponse getEmotionAnalysis(Long userId, String period) {
        List<Dream> dreams =
                dreamRepository.findByUserId(userId, PageRequest.of(0, 100)).getContent();

        Map<String, Integer> emotionTotals = new HashMap<>();
        for (Dream dream : dreams) {
            if (dream.getEmotionScores() != null) {
                dream.getEmotionScores().forEach((k, v) -> emotionTotals.merge(k, v, Integer::sum));
            }
        }

        return EmotionAnalysisResponse.builder()
                .period(period)
                .emotionDistribution(emotionTotals)
                .dailyData(Collections.emptyList())
                .build();
    }

    public PatternAnalysisResponse getDreamPatterns(Long userId) {
        List<Dream> dreams =
                dreamRepository.findByUserId(userId, PageRequest.of(0, 100)).getContent();

        return PatternAnalysisResponse.builder()
                .repeatingSymbols(Collections.emptyList())
                .relationshipPatterns(Collections.emptyList())
                .totalDreamCount(dreams.size())
                .nightmareRatio(0.0)
                .build();
    }
}
