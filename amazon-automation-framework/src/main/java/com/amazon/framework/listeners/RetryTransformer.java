package com.amazon.framework.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * RetryTransformer — applies RetryAnalyzer to ALL tests globally.
 *
 * Without this you'd need @Test(retryAnalyzer=RetryAnalyzer.class) on every method.
 * Register in testng.xml under <listeners> and all tests auto-get retry.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
