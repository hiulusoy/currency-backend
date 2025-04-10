package com.crewmeister.currencybackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to measure and log the execution time of methods.
 * When applied to a method, an aspect will capture the start and end time,
 * calculate the duration, and log the execution time information.
 * <p>
 * Usage:
 *
 * @ExecutionTime public void someMethod() {
 * // Method implementation
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
}
