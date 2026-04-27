package com.amazon.framework.base;

import com.amazon.framework.config.ConfigManager;
import com.amazon.framework.listeners.ExtentReportManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * BaseTest — foundation for ALL test classes.
 *
 * KEY DESIGN DECISIONS:
 *  1. ThreadLocal<WebDriver> — each parallel thread gets its own isolated driver.
 *     Without this, 5 parallel threads would share 1 driver and crash each other.
 *  2. @BeforeMethod / @AfterMethod — driver created fresh per test, destroyed after.
 *  3. Screenshot on failure — captured before driver quits so it's not lost.
 *  4. driverHolder.remove() — prevents memory leaks in long parallel runs.
 */
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    // ─── ThreadLocal: one WebDriver slot per thread ───────────────────────────
    private static final ThreadLocal<WebDriver> driverHolder = new ThreadLocal<>();

    // ─── Public accessor — used by BasePage and all page classes ─────────────
    public static WebDriver getDriver() {
        return driverHolder.get();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUITE LEVEL — runs once for entire test run
    // ─────────────────────────────────────────────────────────────────────────
    @BeforeSuite(alwaysRun = true)
    public void initSuite() {
        log.info("═══ Test Suite Starting ═══");
        log.info("Environment : {}", System.getProperty("env", "local"));
        log.info("Browser     : {}", System.getProperty("browser", "chrome"));
        log.info("Threads     : {}", System.getProperty("threads", "5"));
        ExtentReportManager.initReports();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        ExtentReportManager.flushReports();
        log.info("═══ Test Suite Completed ═══");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD LEVEL — runs before/after EACH test method
    // ─────────────────────────────────────────────────────────────────────────
   

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("TEST FAILED: {}", result.getName());
            captureScreenshot(result.getName());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            log.info("TEST PASSED: {}", result.getName());
        } else {
            log.warn("TEST SKIPPED: {}", result.getName());
        }

        WebDriver driver = driverHolder.get();
        if (driver != null) {
            driver.quit();
            driverHolder.remove(); // CRITICAL: prevents memory leak in thread pool
            log.info("[Thread-{}] Driver quit and removed from ThreadLocal",
                     Thread.currentThread().getId());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCREENSHOT UTILITY
    // ─────────────────────────────────────────────────────────────────────────
    public static String captureScreenshot(String testName) {
        try {
            WebDriver driver = driverHolder.get();
            if (driver == null) return null;

            TakesScreenshot ts = (TakesScreenshot) driver;
            byte[] screenshot   = ts.getScreenshotAs(OutputType.BYTES);

            String dirPath  = "target/screenshots/";
            String fileName = testName + "_" + System.currentTimeMillis() + ".png";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dirPath + fileName);
            Files.write(file.toPath(), screenshot);
            log.info("Screenshot saved: {}", file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            log.error("Failed to capture screenshot", e);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    protected void navigateTo(String url) {
        getDriver().get(url);
        log.info("Navigated to: {}", url);
    }

    protected void navigateToAmazon() {
        navigateTo(ConfigManager.get("base.url"));
    }
    
    public static void setDriver(WebDriver driver) {
        driverHolder.set(driver); // ✅ called from CucumberHooks @Before
    }

    public static void removeDriver() {
        driverHolder.remove(); // ✅ called from CucumberHooks @After
    }
}
