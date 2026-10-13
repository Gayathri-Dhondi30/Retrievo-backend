package com.retrievo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile({"mysql", "postgres", "prod"})
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
        String url = rawUrl;
        String username = rawUsername;
        String password = rawPassword;

        // Automatically convert cloud provider mysql:// URLs into valid JDBC format
        if (url != null && (url.startsWith("mysql://") || url.startsWith("mysql2://"))) {
            try {
                logger.info("Normalizing cloud MySQL URL into JDBC format...");
                URI uri = new URI(url.replace("mysql2://", "http://").replace("mysql://", "http://"));
                
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                String path = uri.getPath();
                
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }
                
                url = "jdbc:mysql://" + host + ":" + port + path + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                logger.info("Normalized JDBC MySQL URL: {}", url);
            } catch (Exception e) {
                logger.warn("Failed to parse MySQL URI, falling back to default URL: {}", e.getMessage());
            }
        } else if (url == null || url.isBlank()) {
            url = "jdbc:mysql://localhost:3306/retrievo_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }

        String finalUsername = (username != null && !username.isBlank()) ? username : "root";
        String finalPassword = (password != null && !password.isBlank()) ? password : "gayathri";

        logger.info("Initializing MySQL DataSource connection to: {}", url);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(finalUsername);
        config.setPassword(finalPassword);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
