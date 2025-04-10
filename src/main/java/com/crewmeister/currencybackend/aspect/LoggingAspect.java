package com.crewmeister.currencybackend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging controller method executions.
 * This class implements cross-cutting concerns for logging using Spring AOP.
 * It logs information about requests to the ExchangeRateController methods.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut definition that targets all methods in the ExchangeRateController class.
     * This pointcut is used to specify where the logging advice should be applied.
     */
    @Pointcut("execution(* com.crewmeister.currencybackend.controller.ExchangeRateController.*(..))")
    public void exchangeRateLoggingPointCut() {
    }

    /**
     * Advice that executes before the targeted controller methods.
     * Logs information about the incoming request including the method signature.
     *
     * @param joinPoint provides access to the executing method information
     */
    @Before("exchangeRateLoggingPointCut()")
    public void exchangeRateLoggingAdvice(JoinPoint joinPoint) {
        log.info("Request to get currency with code: {}", joinPoint.getSignature().toShortString());
    }
}
