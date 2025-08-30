package com.agranelos.inventario.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger logger = Logger.getLogger(
        DatabaseManager.class.getName()
    );
    private static HikariDataSource dataSource;
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (!initialized) {
            initializeDataSource();
            initialized = true;
        }
    }

    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            // Get database configuration from environment variables
            String host = getEnvVariable("DB_HOST");
            String port = getEnvVariable("DB_PORT", "5432");
            String database = getEnvVariable("DB_NAME");
            String username = getEnvVariable("DB_USER");
            String password = getEnvVariable("DB_PASSWORD");

            String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/%s",
                host,
                port,
                database
            );
            logger.info(
                "Connecting to PostgreSQL on EC2: " +
                host +
                ":" +
                port +
                "/" +
                database
            );

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            // Connection pool settings optimized for EC2 PostgreSQL
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(20000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(60000);

            // SSL settings for EC2 PostgreSQL (optional, configurable)
            String sslMode = getEnvVariable("DB_SSL_MODE", "disable");
            config.addDataSourceProperty("sslmode", sslMode);
            if ("require".equals(sslMode) || "prefer".equals(sslMode)) {
                config.addDataSourceProperty("ssl", "true");
            }
            config.addDataSourceProperty("serverTimezone", "UTC");

            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            logger.severe(
                "Failed to initialize database connection: " + e.getMessage()
            );
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return dataSource.getConnection();
    }

    private static String getEnvVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                "Required environment variable not found: " + name
            );
        }
        return value;
    }

    private static String getEnvVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.trim().isEmpty())
            ? value
            : defaultValue;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
        initialized = false;
    }
}
