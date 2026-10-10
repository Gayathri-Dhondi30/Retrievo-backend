package com.retrievo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("postgres")
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String rawUsername;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        String url = rawUrl;
        String username = rawUsername;
        String password = rawPassword;

        // Automatically convert Render/Heroku postgres:// or postgresql:// URLs into valid JDBC format
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                logger.info("Normalizing Render PostgreSQL URL into JDBC format...");
                URI uri = new URI(url.replace("postgresql://", "http://").replace("postgres://", "http://"));
                
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }
                
                url = "jdbc:postgresql://" + host + ":" + port + path + "?sslmode=require";
                logger.info("Normalized JDBC URL: {}", url);
            } catch (Exception e) {
                logger.warn("Failed to parse postgres URI, falling back to original URL: {}", e.getMessage());
            }
        } else if (url != null && url.startsWith("jdbc:postgresql://") && !url.contains("sslmode=")) {
            if (url.contains("?")) {
                url += "&sslmode=require";
            } else {
                url += "?sslmode=require";
            }
        }

        config.setJdbcUrl(url);
        if (username != null && !username.isBlank()) {
            config.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
