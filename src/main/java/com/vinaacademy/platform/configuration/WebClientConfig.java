package com.vinaacademy.platform.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies; // Import mới
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient with load balancing support
 * Used for inter-service communication via Eureka service discovery
 */
@Configuration
@Slf4j
public class WebClientConfig {

    /**
     * Creates a LoadBalanced WebClient.Builder for service-to-service communication
     * This enables automatic service discovery and load balancing through Eureka
     * * @return WebClient.Builder configured with load balancing
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return createWebClientBuilder();
    }

    /**
     * Creates a non-load-balanced WebClient.Builder for direct HTTP/HTTPS URLs
     * Use this for external services or when using direct URLs like http://localhost:8084
     * @return WebClient.Builder configured without load balancing
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return createWebClientBuilder();
    }

    /**
     * Common WebClient.Builder configuration
     * @return Configured WebClient.Builder
     */
    private WebClient.Builder createWebClientBuilder() {
        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        // --- Cấu hình tăng buffer size (Thêm đoạn này) ---
        final int size = 16 * 1024 * 1024; // 16MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        // ------------------------------------------------

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies) // <--- Áp dụng cấu hình strategies vào đây
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse());
    }

    /**
     * Log outgoing requests
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) ->
                        values.forEach(value -> log.debug("{}={}", name, value)));
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * Log incoming responses
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("Response Status Code: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}
