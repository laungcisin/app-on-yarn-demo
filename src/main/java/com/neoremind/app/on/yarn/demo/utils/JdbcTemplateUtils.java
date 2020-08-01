package com.neoremind.app.on.yarn.demo.utils;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JdbcTemplateUtils工具类: 双重加锁检查DCL（Double Check Lock）
 */
public class JdbcTemplateUtils {
    private static volatile JdbcTemplate jdbcTemplate = null;

    private JdbcTemplateUtils() {
    }

    public static JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            synchronized (JdbcTemplateUtils.class) {
                if (jdbcTemplate == null) {
                    HikariDataSource hikariDataSource = HikariUtils.getDataSource();
                    jdbcTemplate = new JdbcTemplate(hikariDataSource);
                }
            }
        }

        return jdbcTemplate;
    }

}
