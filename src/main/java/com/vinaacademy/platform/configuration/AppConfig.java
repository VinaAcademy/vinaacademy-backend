package com.vinaacademy.platform.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class AppConfig {
    public static AppConfig INSTANCE;
    @Getter
    @Value("${application.url.frontend:http://localhost:3000}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
