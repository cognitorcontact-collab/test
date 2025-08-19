package com.example.football.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSourceConfig {

    private final static int defaultPort = 5432;
    private final String host = System.getenv("DATABASE_HOST");
    private final String user = System.getenv("DATABASE_USER");
    private final String password = System.getenv("DATABASE_PASSWORD");
    private final String database = System.getenv("DATABASE_NAME");
    private final String jdbcUrl;

    public DataSourceConfig() {
        jdbcUrl = "jdbc:postgresql://" + host + ":" + defaultPort + "/" + database;
    }

    @Bean
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}