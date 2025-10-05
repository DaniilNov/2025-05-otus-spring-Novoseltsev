package ru.otus.hw.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchInitializer {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeBatchSchema() {
        log.info("=== INITIALIZING SPRING BATCH SCHEMA ===");

        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE", Integer.class);
            log.info("=== BATCH TABLES ALREADY EXIST ===");
        } catch (Exception e) {
            log.info("=== CREATING BATCH TABLES FROM SPRING BATCH SCHEMA ===");
            try {
                ScriptUtils.executeSqlScript(
                        dataSource.getConnection(),
                        new ClassPathResource("org/springframework/batch/core/schema-h2.sql")
                );
                log.info("=== BATCH SCHEMA CREATED SUCCESSFULLY ===");
            } catch (Exception ex) {
                log.error("=== ERROR CREATING BATCH SCHEMA ===", ex);
                throw new RuntimeException("Failed to initialize batch schema", ex);
            }
        }
    }
}