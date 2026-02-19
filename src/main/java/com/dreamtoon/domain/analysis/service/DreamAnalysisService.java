package com.dreamtoon.domain.analysis.service;

import com.dreamtoon.domain.analysis.dto.DreamAnalysisResponse;
import com.dreamtoon.domain.analysis.dto.DreamAnalysisResponse.EmotionBalance;
import com.dreamtoon.domain.analysis.dto.DreamAnalysisResponse.WeeklyDreamStat;
import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.EmotionType;
import com.dreamtoon.domain.dream.repository.DreamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DreamAnalysisService {

    private final DreamRepository dreamRepository;

    @Transactional(readOnly = true)
    public DreamAnalysisResponse getDashboardAnalysis(Long userId, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Dream> dreams =
                dreamRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);

        if (dreams.isEmpty()) {
            return DreamAnalysisResponse.builder()
                    .hasEnoughData(false)
                    .message("아직 분석할 꿈 데이터가 없어요. 첫 번째 꿈을 기록해보세요!")
                    .stressIndex(0)
                    .sleepQualityScore(0)
                    .emotionBalance(null)
                    .weeklyDreamFlow(new ArrayList<>())
                    .build();
        }

        int stressIndex = calculateStressIndex(dreams);
        int sleepQualityScore = calculateSleepQuality(stressIndex);
        EmotionBalance emotionBalance = calculateEmotionBalance(dreams);
        List<WeeklyDreamStat> weeklyDreamFlow = getWeeklyDreamFlow(dreams, days);
        String aiCoachMessage = generateAiCoachMessage(stressIndex, emotionBalance);

        return DreamAnalysisResponse.builder()
                .hasEnoughData(true)
                .stressIndex(stressIndex)
                .stressLevel(getStressLevel(stressIndex))
                .sleepQualityScore(sleepQualityScore)
                .sleepQualityMessage(getSleepQualityMessage(sleepQualityScore))
                .emotionBalance(emotionBalance)
                .weeklyDreamFlow(weeklyDreamFlow)
                .aiCoachMessage(aiCoachMessage)
                .build();
    }

    private int calculateStressIndex(List<Dream> dreams) {
        if (dreams.isEmpty()) return 0;

        double totalStress = 0;
        int count = 0;

        for (Dream dream : dreams) {
            Map<String, Integer> scores = dream.getEmotionScores();
            if (scores == null || scores.isEmpty()) continue;

            // 가중치 적용: 불안(1.0), 분노(1.0), 슬픔(0.5)
            double dreamStress =
                    getEmotionScore(scores, EmotionType.ANXIETY) * 1.0
                            + getEmotionScore(scores, EmotionType.ANGER) * 1.0
                            + getEmotionScore(scores, EmotionType.SADNESS) * 0.5;

            // 최대 300점이 나올 수 있으므로 100점으로 정규화 (대략 3으로 나눔)
            // 하지만 엄밀한 통계보다 경향성이 중요하므로 단순 평균 사용 후 조정
            // 여기서는 최대 100점으로 제한
            totalStress += Math.min(dreamStress, 100);
            count++;
        }

        return count == 0 ? 0 : (int) (totalStress / count);
    }

    private int calculateSleepQuality(int stressIndex) {
        // 스트레스가 높으면 수면 질이 낮다고 가정 (역산)
        // 기본 80점에서 스트레스 지수의 50%만큼 차감
        int score = 80 - (stressIndex / 2);
        return Math.max(0, Math.min(100, score));
    }

    private String getStressLevel(int stressIndex) {
        if (stressIndex >= 70) return "HIGH";
        if (stressIndex >= 40) return "MEDIUM";
        return "LOW";
    }

    private String getSleepQualityMessage(int score) {
        if (score >= 80) return "숙면을 취하고 계시네요!";
        if (score >= 50) return "평범한 수면 상태입니다.";
        return "수면의 질 개선이 필요해요.";
    }

    private EmotionBalance calculateEmotionBalance(List<Dream> dreams) {
        int joy = 0, anxiety = 0, anger = 0, sadness = 0, discomfort = 0, peace = 0;
        int count = 0;

        for (Dream dream : dreams) {
            Map<String, Integer> scores = dream.getEmotionScores();
            if (scores == null) continue;

            joy += getEmotionScore(scores, EmotionType.JOY);
            anxiety += getEmotionScore(scores, EmotionType.ANXIETY);
            anger += getEmotionScore(scores, EmotionType.ANGER);
            sadness += getEmotionScore(scores, EmotionType.SADNESS);
            peace += getEmotionScore(scores, EmotionType.PEACE);
            count++;
        }

        if (count == 0) return EmotionBalance.builder().build();

        // 평균값 계산
        return EmotionBalance.builder()
                .joy(joy / count)
                .anxiety(anxiety / count)
                .anger(anger / count)
                .sadness(sadness / count)
                .discomfort(discomfort / count)
                .peace(peace / count)
                .build();
    }

    private List<WeeklyDreamStat> getWeeklyDreamFlow(List<Dream> dreams, int days) {
        List<WeeklyDreamStat> stats = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 지난 days일 동안의 날짜를 생성
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            // 해당 날짜의 꿈 찾기 (가장 최근 것 하나)
            Dream dailyDream =
                    dreams.stream()
                            .filter(d -> d.getCreatedAt().toLocalDate().isEqual(date))
                            .findFirst() // 리스트는 이미 최신순 정렬되어 있음
                            .orElse(null);

            stats.add(
                    WeeklyDreamStat.builder()
                            .date(date)
                            .hasDream(dailyDream != null)
                            .primaryEmotion(
                                    dailyDream != null && dailyDream.getPrimaryEmotion() != null
                                            ? dailyDream.getPrimaryEmotion().getDescription()
                                            : null)
                            .build());
        }
        return stats;
    }

    private String generateAiCoachMessage(int stressIndex, EmotionBalance balance) {
        if (stressIndex >= 70) {
            return "최근 스트레스 지수가 높습니다. 자기 전 명상이나 가벼운 스트레칭을 추천드려요.";
        }
        if (balance.getAnxiety() > 50) {
            return "불안감이 다소 높은 편입니다. 걱정거리를 메모장에 적어두고 잊어보세요.";
        }
        if (balance.getJoy() > 50 || balance.getPeace() > 50) {
            return "긍정적인 에너지가 가득하네요! 이 기분을 유지하며 하루를 시작해보세요.";
        }
        return "평온한 상태를 유지하고 계십니다.";
    }

    // GPT가 반환하는 한글 키 중 EmotionType.description과 다른 변형
    private static final Map<String, String> EMOTION_ALIASES =
            Map.of(
                    "놀람", "SURPRISE",
                    "불편", "ANXIETY");

    /** code(소문자 영어), description(한글), 별칭 모두로 감정 점수 조회 */
    private int getEmotionScore(Map<String, Integer> scores, EmotionType type) {
        int value =
                scores.getOrDefault(type.getCode(), scores.getOrDefault(type.getDescription(), 0));
        // 별칭에서 추가 점수 합산 (예: ANXIETY ← "불편", SURPRISE ← "놀람")
        for (Map.Entry<String, String> alias : EMOTION_ALIASES.entrySet()) {
            if (alias.getValue().equals(type.name())) {
                value += scores.getOrDefault(alias.getKey(), 0);
            }
        }
        return value;
    }
}
