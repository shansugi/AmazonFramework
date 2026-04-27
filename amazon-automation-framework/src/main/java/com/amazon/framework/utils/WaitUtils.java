package com.amazon.framework.utils;

import com.amazon.framework.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.NoSuchElementException;

/**
 * WaitUtils — advanced wait strategies beyond BasePage defaults.
 * Use for polling, dynamic elements, and AJAX-heavy pages like Amazon.
 */
public class WaitUtils {

    private WaitUtils() {}

    /**
     * FluentWait — polls every 500ms, ignores NoSuchElementException.
     * Use for elements that appear/disappear dynamically (cart count, loaders).
     */
    public static WebElement fluentWait(By locator, int timeoutSeconds) {
        WebDriver driver = BaseTest.getDriver();
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .until(d -> d.findElement(locator));
    }

    /**
     * Wait for page title to contain a string.
     * Useful after navigation to verify correct page loaded.
     */
    public static boolean waitForTitle(String titleFragment, int seconds) {
        return new WebDriverWait(BaseTest.getDriver(), Duration.ofSeconds(seconds))
                .until(d -> d.getTitle().toLowerCase().contains(titleFragment.toLowerCase()));
    }

    /**
     * Wait for URL to contain a fragment.
     * Useful after checkout redirect, login redirect etc.
     */
    public static boolean waitForUrlContains(String fragment, int seconds) {
        return new WebDriverWait(BaseTest.getDriver(), Duration.ofSeconds(seconds))
                .until(d -> d.getCurrentUrl().contains(fragment));
    }

    /**
     * Wait for element count to reach expected number.
     * Useful for product listing pages where results load progressively.
     */
    public static boolean waitForElementCount(By locator, int expectedCount, int seconds) {
        return new WebDriverWait(BaseTest.getDriver(), Duration.ofSeconds(seconds))
                .until(d -> d.findElements(locator).size() >= expectedCount);
    }

    /**
     * Hard sleep — ONLY use when a hard browser animation or redirect
     * cannot be detected by any ExpectedCondition.
     * Document WHY you're using this if you do.
     */
    public static void hardWait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
