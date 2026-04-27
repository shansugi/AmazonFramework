package com.amazon.framework.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer — auto-retries failed tests up to MAX_RETRY times.
 *
 * Wire it at test level:  @Test(retryAnalyzer = RetryAnalyzer.class)
 * Wire it globally:       via RetryTransformer listener in testng.xml
 *
 * Reduces false negatives from network blips, timing issues, and
 * environment instability — without hiding real failures.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 2;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("Retrying test '{}' — attempt {}/{} | Failure: {}",
                     result.getName(), retryCount, MAX_RETRY,
                     result.getThrowable().getMessage());
            return true;
        }
        log.error("Test '{}' failed after {} retries.", result.getName(), MAX_RETRY);
        return false;
    }
}
