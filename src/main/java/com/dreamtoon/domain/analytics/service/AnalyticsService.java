package com.dreamtoon.domain.analytics.service;

import com.dreamtoon.domain.analytics.dto.EmotionAnalysisResponse;
import com.dreamtoon.domain.analytics.dto.HealthIndexResponse;
import com.dreamtoon.domain.analytics.dto.PatternAnalysisResponse;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import java.util.ArrayList;
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

        Map<String, Integer> rawTotals = new HashMap<>();
        int count = 0;
        for (Dream dream : dreams) {
            if (dream.getEmotionScores() != null) {
                dream.getEmotionScores().forEach((k, v) -> rawTotals.merge(k, v, Integer::sum));
                count++;
            }
        }

        Map<String, Integer> emotionTotals = normalizeEmotionKeys(rawTotals);

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

        Map<String, Integer> rawTotals = new HashMap<>();
        for (Dream dream : dreams) {
            if (dream.getEmotionScores() != null) {
                dream.getEmotionScores().forEach((k, v) -> rawTotals.merge(k, v, Integer::sum));
            }
        }

        Map<String, Integer> emotionTotals = normalizeEmotionKeys(rawTotals);

        List<EmotionAnalysisResponse.DailyEmotionData> dailyData = new ArrayList<>();
        for (Dream dream : dreams) {
            if (dream.getCreatedAt() == null) continue;
            dailyData.add(
                    EmotionAnalysisResponse.DailyEmotionData.builder()
                            .date(dream.getCreatedAt().toLocalDate().toString())
                            .primaryEmotion(
                                    dream.getPrimaryEmotion() != null
                                            ? dream.getPrimaryEmotion().getDescription()
                                            : "평온")
                            .sleepScore(calculateSleepScore(dream.getEmotionScores()))
                            .build());
        }

        return EmotionAnalysisResponse.builder()
                .period(period)
                .emotionDistribution(emotionTotals)
                .dailyData(dailyData)
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

    // GPT가 반환하는 한글 키 중 EmotionType.description과 다른 변형
    private static final Map<String, String> EMOTION_ALIASES =
            Map.of(
                    "놀람", "SURPRISE", // EmotionType.SURPRISE.description = "놀라움"
                    "불편", "ANXIETY" // "불편"(discomfort)은 불안 계열로 합산
                    );

    /** 한글/소문자 감정 키를 영문 대문자(EmotionType.name())로 정규화 */
    private Map<String, Integer> normalizeEmotionKeys(Map<String, Integer> raw) {
        Map<String, Integer> normalized = new HashMap<>();
        for (Map.Entry<String, Integer> e : raw.entrySet()) {
            String key = e.getKey();
            String normalizedKey = key;

            // 1) EmotionType enum의 description/code로 매칭
            boolean matched = false;
            for (EmotionType type : EmotionType.values()) {
                if (type.getDescription().equals(key) || type.getCode().equals(key)) {
                    normalizedKey = type.name();
                    matched = true;
                    break;
                }
            }
            // 2) 별칭 매칭 (놀람, 불편 등)
            if (!matched && EMOTION_ALIASES.containsKey(key)) {
                normalizedKey = EMOTION_ALIASES.get(key);
            }

            normalized.merge(normalizedKey, e.getValue(), Integer::sum);
        }
        return normalized;
    }

    /** 감정 점수 기반 수면 점수 계산 (1~5) */
    private int calculateSleepScore(Map<String, Integer> emotionScores) {
        if (emotionScores == null || emotionScores.isEmpty()) return 3;

        Map<String, Integer> norm = normalizeEmotionKeys(emotionScores);
        int negative =
                norm.getOrDefault("ANXIETY", 0)
                        + norm.getOrDefault("ANGER", 0)
                        + norm.getOrDefault("SADNESS", 0);
        int positive = norm.getOrDefault("JOY", 0) + norm.getOrDefault("PEACE", 0);

        // 부정 감정이 높을수록 수면 점수 낮음, 긍정 감정이 높을수록 높음
        int score = 3;
        if (positive > negative + 30) score = 5;
        else if (positive > negative) score = 4;
        else if (negative > positive + 60) score = 1;
        else if (negative > positive + 20) score = 2;
        return score;
    }
}
