package com.neoremind.app.on.yarn.demo.utils;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Hikari工具类: 双重加锁检查DCL（Double Check Lock）
 */
public class HikariUtils {
    private static volatile HikariDataSource hikariDataSource = null;

    private HikariUtils() {
    }

    public static HikariDataSource getDataSource() {
        if (hikariDataSource == null) {
            synchronized (HikariUtils.class) {
                if (hikariDataSource == null) {
                    hikariDataSource = new HikariDataSource();
//                    hikariDataSource.setDriverClassName("com.mysql.jdbc.Driver");
                    hikariDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
                    hikariDataSource.setUsername("root");
                    hikariDataSource.setPassword("123456");
                    hikariDataSource.setMaximumPoolSize(10);
                    hikariDataSource.setMinimumIdle(5);
                    // 是否自定义配置，为true时下面两个参数才生效
//                    hikariDataSource.addDataSourceProperty("cachePrepStmts", true);
                    // 连接池大小默认25，官方推荐250-500
//                    hikariDataSource.addDataSourceProperty("prepStmtCacheSize", 250);
                    // 单条语句最大长度默认256，官方推荐2048
//                    hikariDataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
                }
            }
        }

        return hikariDataSource;
    }

}
