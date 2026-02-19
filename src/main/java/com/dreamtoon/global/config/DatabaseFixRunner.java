package com.dreamtoon.global.config;

import com.dreamtoon.domain.dream.entity.Genre;
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
        String[] migrations = {
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS cancel_at_period_end boolean NOT"
                    + " NULL DEFAULT false",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS premium_generation_count integer"
                    + " NOT NULL DEFAULT 0",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS premium_trial_used boolean NOT NULL"
                    + " DEFAULT false",
            "ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS standard_generation_count integer"
                    + " NOT NULL DEFAULT 0",
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
