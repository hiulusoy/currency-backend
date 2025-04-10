package com.crewmeister.currencybackend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for measuring and logging method execution time.
 * This aspect applies to methods annotated with the @ExecutionTime annotation
 * and provides performance monitoring capabilities.
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    /**
     * Pointcut that targets methods annotated with @ExecutionTime.
     * This defines where the performance monitoring should be applied.
     */
    @Pointcut("@annotation(com.crewmeister.currencybackend.annotation.ExecutionTime)")
    public void executionTimePC() {
    }

    /**
     * Around advice that measures and logs the execution time of annotated methods.
     * This advice runs before and after the method execution to calculate duration.
     *
     * @param proceedingJoinPoint provides access to the executing method
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("executionTimePC()")
    public Object aroundAnyExecutionTimeAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // Record start time
        long startTime = System.currentTimeMillis();

        // Get method signature for better logging
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        // Log method execution start with parameters (for debugging)
        if (log.isDebugEnabled()) {
            log.debug("Executing: {}.{}() with parameters: {}",
                    className, methodName, Arrays.toString(proceedingJoinPoint.getArgs()));
        } else {
            log.info("Execution starts: {}.{}()", className, methodName);
        }

        Object result = null;
        try {
            // Execute the actual method
            result = proceedingJoinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            log.error("Exception in {}.{}(): {}", className, methodName, throwable.getMessage());
            throw throwable;
        } finally {
            // Calculate and log execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Use different log levels based on execution time
            if (executionTime > 1000) {
                log.warn("SLOW EXECUTION - Time taken to execute {}.{}(): {} ms",
                        className, methodName, executionTime);
            } else {
                log.info("Time taken to execute {}.{}(): {} ms",
                        className, methodName, executionTime);
            }
        }
    }
}
