package com.amazon.hooks;

import com.amazon.framework.base.BaseTest;
import com.amazon.framework.factory.DriverFactory;
import com.amazon.framework.listeners.ExtentReportManager;
import io.cucumber.java.*;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * CucumberHooks — Before/After hooks for every Cucumber scenario.
 *
 * These fire around each scenario. Used for:
 * - Logging scenario start/end
 * - Attaching screenshots on failure to Extent Report
 * - Any scenario-level setup/teardown not in @BeforeMethod
 */
public class CucumberHooks {

    private static final Logger log = LogManager.getLogger(CucumberHooks.class);

    @Before
    public void beforeScenario(Scenario scenario) {
        // ✅ CREATE driver here — before any step runs
        WebDriver driver = DriverFactory.createDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().maximize();
        BaseTest.setDriver(driver); // ✅ store in ThreadLocal

        log.info("▶ Scenario: {} | Tags: {}", 
            scenario.getName(), scenario.getSourceTagNames());
        ExtentReportManager.logStep("Starting scenario: " + scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("✘ Scenario FAILED: {}", scenario.getName());
            try {
                byte[] screenshot = ((TakesScreenshot) BaseTest.getDriver())
                        .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
                ExtentReportManager.logFail("Scenario failed — screenshot attached");
            } catch (Exception e) {
                log.warn("Could not attach screenshot to scenario", e);
            }
        } else {
            log.info("✔ Scenario PASSED: {}", scenario.getName());
            ExtentReportManager.logPass("Scenario passed: " + scenario.getName());
        }

        // ✅ QUIT driver after every scenario
        WebDriver driver = BaseTest.getDriver();
        if (driver != null) {
            driver.quit();
            BaseTest.removeDriver(); // ✅ clean up ThreadLocal
            log.info("Driver quit after scenario: {}", scenario.getName());
        }
    }

    @BeforeStep
    public void beforeStep(Scenario scenario) {
        // Uncomment if you want per-step logging
        // log.debug("Step starting in: {}", scenario.getName());
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            // Screenshot after each failed step for granular debugging
            try {
                byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) BaseTest.getDriver())
                        .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Step Failure");
            } catch (Exception ignored) {}
        }
    }
}
