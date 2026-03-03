package com.dreamtoon.global.config;

import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.user.entity.SocialProvider;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseFixRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and updating database constraints...");

        // subscriptions 테이블 컬럼 마이그레이션 (NOT NULL 컬럼 추가 시 DEFAULT 필요)
        migrateSubscriptionsColumns();

        // social_provider CHECK constraint 업데이트 (LOCAL 추가)
        try {
            String dropProviderSql =
                    "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_social_provider_check";
            jdbcTemplate.execute(dropProviderSql);

            String providerValues =
                    Arrays.stream(SocialProvider.values())
                            .map(Enum::name)
                            .collect(Collectors.joining("', '", "'", "'"));
            String addProviderSql =
                    String.format(
                            "ALTER TABLE users ADD CONSTRAINT users_social_provider_check CHECK"
                                    + " (social_provider IN (%s))",
                            providerValues);
            jdbcTemplate.execute(addProviderSql);
            log.info(
                    "Updated constraint 'users_social_provider_check' with values: {}",
                    providerValues);
        } catch (Exception e) {
            log.warn("social_provider constraint 업데이트 스킵: {}", e.getMessage());
        }

        try {
            // 1. Drop existing constraint
            String dropSql =
                    "ALTER TABLE dreams DROP CONSTRAINT IF EXISTS dreams_selected_genre_check";
            jdbcTemplate.execute(dropSql);
            log.info("Dropped existing constraint 'dreams_selected_genre_check'");

            // 2. Construct new constraint SQL
            String allowedValues =
                    Arrays.stream(Genre.values())
                            .map(Enum::name)
                            .collect(Collectors.joining("', '", "'", "'"));

            String addSql =
                    String.format(
                            "ALTER TABLE dreams ADD CONSTRAINT dreams_selected_genre_check CHECK"
                                    + " (selected_genre IN (%s))",
                            allowedValues);

            // 3. Add new constraint
            jdbcTemplate.execute(addSql);
            log.info(
                    "Added updated constraint 'dreams_selected_genre_check' with values: {}",
                    allowedValues);

        } catch (Exception e) {
            log.error("Failed to update database constraints: {}", e.getMessage());
            // Do not rethrow to avoid preventing app startup, but log error
        }
    }

    private void migrateSubscriptionsColumns() {
        // 먼저 기존 NULL 값을 DEFAULT로 채우기 (컬럼이 이미 존재하지만 NULL인 경우)
        String[] nullFixes = {
            "UPDATE subscriptions SET cancel_at_period_end = false WHERE cancel_at_period_end IS"
                    + " NULL",
            "UPDATE subscriptions SET premium_generation_count = 0 WHERE premium_generation_count"
                    + " IS NULL",
            "UPDATE subscriptions SET premium_trial_used = false WHERE premium_trial_used IS NULL",
            "UPDATE subscriptions SET standard_generation_count = 0 WHERE standard_generation_count"
                    + " IS NULL",
            "UPDATE subscriptions SET library_count = 0 WHERE library_count IS NULL",
            "UPDATE subscriptions SET favorite_count = 0 WHERE favorite_count IS NULL",
        };
        for (String sql : nullFixes) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                // 컬럼이 아직 없으면 무시
            }
        }

        String[] migrations = {
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS cancel_at_period_end boolean NOT"
                    + " NULL DEFAULT false",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS premium_generation_count integer"
                    + " NOT NULL DEFAULT 0",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS premium_trial_used boolean NOT NULL"
                    + " DEFAULT false",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS standard_generation_count integer"
                    + " NOT NULL DEFAULT 0",
            // 구 generation_count 컬럼이 DB에 남아있는 경우 DEFAULT 0 설정 (엔티티에서 제거된 컬럼)
            "ALTER TABLE subscriptions ALTER COLUMN generation_count SET DEFAULT 0",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS library_count integer NOT NULL"
                    + " DEFAULT 0",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS favorite_count integer NOT NULL"
                    + " DEFAULT 0",
        };
        for (String sql : migrations) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.warn("subscriptions 컬럼 마이그레이션 스킵: {}", e.getMessage());
            }
        }
        log.info("subscriptions 컬럼 마이그레이션 완료");
    }
}
