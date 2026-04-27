package com.amazon.framework.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.amazon.framework.base.BaseTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

/**
 * ExtentReportManager — manages Extent Reports lifecycle.
 *
 * ThreadLocal<ExtentTest> ensures each parallel thread writes to its OWN
 * test node in the report — no race conditions, no mixed-up logs.
 *
 * Attach this as a listener in testng.xml:
 *   <listeners>
 *     <listener class-name="com.amazon.framework.listeners.ExtentReportManager"/>
 *   </listeners>
 */
public class ExtentReportManager implements ITestListener {

    private static final Logger log = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;

    // ThreadLocal: each parallel thread writes to its own ExtentTest node
    private static final ThreadLocal<ExtentTest> testHolder = new ThreadLocal<>();

    public static ExtentTest getTest() { return testHolder.get(); }

    // ─────────────────────────────────────────────────────────────────────────
    // INIT — called once from BaseTest @BeforeSuite
    // ─────────────────────────────────────────────────────────────────────────
    public static synchronized void initReports() {
        if (extent != null) return;

        String reportPath = "target/extent-reports/AmazonTestReport.html";
        new File("target/extent-reports").mkdirs();
        new File("target/screenshots").mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Amazon Automation Report");
        spark.config().setReportName("Amazon E2E Test Results");
        spark.config().setTheme(Theme.DARK);
        spark.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Environment", System.getProperty("env", "local"));
        extent.setSystemInfo("Browser",     System.getProperty("browser", "chrome"));
        extent.setSystemInfo("OS",          System.getProperty("os.name"));
        extent.setSystemInfo("Java",        System.getProperty("java.version"));
        extent.setSystemInfo("Threads",     System.getProperty("threads", "5"));

        log.info("Extent Reports initialized: {}", reportPath);
    }

    public static synchronized void flushReports() {
        if (extent != null) {
            extent.flush();
            log.info("Extent Reports flushed and saved.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTNG LISTENER HOOKS
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName()
                .replaceAll(".*\\.", ""); // simple class name

        ExtentTest test = extent.createTest(testName)
                .assignCategory(className)
                .assignDevice(System.getProperty("browser", "chrome"));
        testHolder.set(test);

        log.info("▶ Test started: {} [Thread-{}]",
                 testName, Thread.currentThread().getId());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = getTest();

        // ✅ Add null check
        if (test == null) {
            test = extent.createTest(result.getName()); // create fresh if missing
            testHolder.set(test);
        }

        test.log(Status.FAIL, "Test failed: " + result.getThrowable().getMessage());
        test.fail(result.getThrowable());

        String screenshotPath = BaseTest.captureScreenshot(result.getName());
        if (screenshotPath != null) {
            try {
                test.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            } catch (Exception e) {
                log.error("Could not attach screenshot to report", e);
            }
        }

        log.error("✘ FAIL: {}", result.getName());
        testHolder.remove();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = getTest();

        // ✅ Add null check
        if (test == null) {
            test = extent.createTest(result.getName());
            testHolder.set(test);
        }

        test.log(Status.PASS, "Test passed: " + result.getName());
        log.info("✔ PASS: {}", result.getName());
        testHolder.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = getTest();

        // ✅ Add null check
        if (test == null) {
            test = extent.createTest(result.getName());
            testHolder.set(test);
        }

        test.log(Status.SKIP, "Test skipped: " + result.getName());
        log.warn("⊘ SKIP: {}", result.getName());
        testHolder.remove();
    }

    // Log step-level info — called from step definitions and page methods
    public static void logStep(String message) {
        ExtentTest test = testHolder.get();
        if (test != null) test.log(Status.INFO, message);
        log.info(message);
    }

    public static void logPass(String message) {
        ExtentTest test = testHolder.get();
        if (test != null) test.log(Status.PASS, message);
        log.info("PASS: {}", message);
    }

    public static void logFail(String message) {
        ExtentTest test = testHolder.get();
        if (test != null) test.log(Status.FAIL, message);
        log.error("FAIL: {}", message);
    }
}
