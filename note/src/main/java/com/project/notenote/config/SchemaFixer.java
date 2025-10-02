package com.project.notenote.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures DB schema matches application expectations at startup.
 * Specifically, makes dates.end_date nullable so cards can be created with no due date.
 */
@Component
public class SchemaFixer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaFixer.class);
    private final JdbcTemplate jdbc;

    public SchemaFixer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Check current nullability of dates.end_date
            String isNullable = jdbc.queryForObject(
                "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dates' AND COLUMN_NAME = 'end_date'",
                String.class
            );
            if (isNullable != null && isNullable.equalsIgnoreCase("NO")) {
                log.warn("Altering table 'dates': making column 'end_date' NULLABLE to match entity mapping and timezone handling...");
                try {
                    // Use TIMESTAMP NULL; MySQL stores in UTC and converts to session time zone
                    jdbc.execute("ALTER TABLE dates MODIFY end_date TIMESTAMP NULL");
                } catch (Exception e1) {
                    log.warn("Failed to alter as TIMESTAMP, retry with DATETIME: {}", e1.getMessage());
                    try { jdbc.execute("ALTER TABLE dates MODIFY end_date DATETIME NULL"); }
                    catch (Exception e2) { log.error("Failed to alter end_date to NULLABLE: {}", e2.getMessage()); }
                }
            }
        } catch (Exception e) {
            // Best-effort; don't block app startup
            log.debug("Schema check/adjust skipped or failed: {}", e.getMessage());
        }
    }
}
