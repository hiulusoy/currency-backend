package com.crewmeister.currencybackend.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for Resilience4j Retry mechanism.
 * <p>
 * This class configures the Retry pattern implementation using Resilience4j.
 * Retry pattern allows the application to automatically retry failed operations
 * with a configurable backoff strategy.
 */
@Configuration
public class RetryConfiguration {  // Sınıf adını değiştirdik

    /**
     * Creates a RetryRegistry with default configurations.
     * <p>
     * This configuration:
     * - Attempts each operation up to 3 times
     * - Waits 1 second between retry attempts
     * - Retries on all exceptions except IllegalArgumentException
     *
     * @return a RetryRegistry with the default configuration
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()  // Bu io.github.resilience4j.retry.RetryConfig sınıfının static metodudur
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return RetryRegistry.of(config);  // Bu io.github.resilience4j.retry.RetryRegistry sınıfının static metodudur
    }
}
