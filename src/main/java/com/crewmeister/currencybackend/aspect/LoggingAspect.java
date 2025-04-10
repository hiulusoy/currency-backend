package com.crewmeister.currencybackend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.crewmeister.currencybackend.controller.ExchangeRateController.*(..))")
    public void exchangeRateLoggingPointCut() {
    }


    @Before("exchangeRateLoggingPointCut()")
    public void exchangeRateLoggingAdvice(JoinPoint joinPoint) {
        log.info("Request to get currency with code: {}", joinPoint.getSignature().toShortString());
    }

}
