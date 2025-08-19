package com.football.centralapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CentralApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CentralApiApplication.class, args);
    }
}