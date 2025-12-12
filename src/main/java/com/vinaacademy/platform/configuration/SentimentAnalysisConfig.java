package com.vinaacademy.platform.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Configuration for sentiment analysis features
 * - RestTemplate for Lang-AI Service communication
 * - Async executor for non-blocking sentiment analysis
 */
@Configuration
@EnableAsync
@Slf4j
public class SentimentAnalysisConfig {

    @Value("${langai.service.timeout:10}")
    private int timeoutSeconds;

    /**
     * RestTemplate bean for HTTP communication with Lang-AI Service
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(timeoutSeconds))
            .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }

    /**
     * Async executor for sentiment analysis tasks
     * Prevents blocking main thread when analyzing reviews
     */
    @Bean(name = "sentimentAnalysisExecutor")
    public Executor sentimentAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size: số threads luôn chạy
        executor.setCorePoolSize(5);
        
        // Max pool size: số threads tối đa
        executor.setMaxPoolSize(10);
        
        // Queue capacity: số tasks chờ trong hàng đợi
        executor.setQueueCapacity(100);
        
        // Thread name prefix
        executor.setThreadNamePrefix("sentiment-async-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("Initialized sentiment analysis async executor: core={}, max={}, queue={}", 
            executor.getCorePoolSize(), 
            executor.getMaxPoolSize(), 
            executor.getQueueCapacity());
        
        return executor;
    }
}
