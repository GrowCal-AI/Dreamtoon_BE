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
}
