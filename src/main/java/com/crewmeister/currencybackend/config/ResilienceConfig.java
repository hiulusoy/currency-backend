package com.crewmeister.currencybackend.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for Resilience4j Circuit Breaker.
 * <p>
 * This class configures the Circuit Breaker pattern implementation using Resilience4j.
 * Circuit Breaker prevents cascading failures by detecting when external services
 * are failing and temporarily stopping requests to them.
 */
@Configuration
public class ResilienceConfig {

    /**
     * Creates a CircuitBreakerRegistry with default configurations.
     * <p>
     * This configuration:
     * - Uses a count-based sliding window of 10 calls to determine circuit state
     * - Opens the circuit when 50% of calls fail
     * - Keeps the circuit open for 10 seconds before trying again
     * - Allows 5 calls in half-open state to determine if service is recovered
     *
     * @return a CircuitBreakerRegistry with the default configuration
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    /**
     * Creates a TimeLimiterConfig for timeout handling.
     * <p>
     * Sets a 4-second timeout for all calls protected by the time limiter
     *
     * @return a TimeLimiterConfig with the default configuration
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4))
                .build();
    }
}
