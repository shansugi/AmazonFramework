package com.amazon.framework.factory;

import com.amazon.framework.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * DriverFactory — reads -Denv system property and returns the correct WebDriver.
 *
 * Usage:
 *   -Denv=local   → ChromeDriver on developer machine
 *   -Denv=grid    → RemoteWebDriver → Docker Selenium Grid hub
 *   -Denv=cloud   → RemoteWebDriver → BrowserStack (real devices, Safari)
 *
 * Test classes NEVER call this directly — BaseTest manages the lifecycle.
 */
public class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    private static final String ENV     = System.getProperty("env",     "local");
    private static final String BROWSER = System.getProperty("browser", "chrome");
    private static final String HUB_URL = System.getProperty("hub.url", "http://localhost:4444/wd/hub");
    private static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "false"));

    private DriverFactory() {}

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT — called by BaseTest @BeforeMethod
    // ─────────────────────────────────────────────────────────
    public static WebDriver createDriver() {
        log.info("Creating driver | env={} | browser={} | headless={}", ENV, BROWSER, HEADLESS);
        return switch (ENV.toLowerCase()) {
            case "grid"  -> createGridDriver();
            case "cloud" -> createBrowserStackDriver();
            default      -> createLocalDriver();
        };
    }

    // ─────────────────────────────────────────────────────────
    // LOCAL — developer machine
    // ─────────────────────────────────────────────────────────
    private static WebDriver createLocalDriver() {
        return switch (BROWSER.toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                yield new FirefoxDriver(buildFirefoxOptions());
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                yield new EdgeDriver(buildEdgeOptions());
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                yield new ChromeDriver(buildChromeOptions());
            }
        };
    }

    // ─────────────────────────────────────────────────────────
    // GRID — Docker Selenium Grid (RemoteWebDriver)
    // ─────────────────────────────────────────────────────────
    private static WebDriver createGridDriver() {
        try {
            URL hub = new URL(HUB_URL);
            log.info("Connecting to Selenium Grid at: {}", HUB_URL);
            return switch (BROWSER.toLowerCase()) {
                case "firefox" -> new RemoteWebDriver(hub, buildFirefoxOptions());
                case "edge"    -> new RemoteWebDriver(hub, buildEdgeOptions());
                default        -> new RemoteWebDriver(hub, buildChromeOptions());
            };
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Selenium Grid hub URL: " + HUB_URL, e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // CLOUD — BrowserStack (real devices, Safari, cross-OS)
    // ─────────────────────────────────────────────────────────
    private static WebDriver createBrowserStackDriver() {
        String user = System.getenv("BS_USERNAME");
        String key  = System.getenv("BS_ACCESS_KEY");

        if (user == null || key == null) {
            throw new RuntimeException(
                "BrowserStack credentials missing. Set BS_USERNAME and BS_ACCESS_KEY env vars.");
        }

        try {
            String bsUrl = "https://" + user + ":" + key + "@hub-cloud.browserstack.com/wd/hub";
            log.info("Connecting to BrowserStack | browser={}", BROWSER);
            return new RemoteWebDriver(new URL(bsUrl), buildBrowserStackCapabilities());
        } catch (MalformedURLException e) {
            throw new RuntimeException("BrowserStack URL construction failed", e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // OPTION BUILDERS
    // ─────────────────────────────────────────────────────────
    private static ChromeOptions buildChromeOptions() {
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--no-sandbox");             // required in Docker
        opts.addArguments("--disable-dev-shm-usage"); // prevents crashes in Docker
        opts.addArguments("--window-size=1920,1080");
        opts.addArguments("--disable-gpu");
        opts.addArguments("--disable-extensions");
        if (HEADLESS) opts.addArguments("--headless=new");
        return opts;
    }

    private static FirefoxOptions buildFirefoxOptions() {
        FirefoxOptions opts = new FirefoxOptions();
        opts.addArguments("--width=1920", "--height=1080");
        if (HEADLESS) opts.addArguments("-headless");
        return opts;
    }

    private static EdgeOptions buildEdgeOptions() {
        EdgeOptions opts = new EdgeOptions();
        opts.addArguments("--no-sandbox");
        opts.addArguments("--window-size=1920,1080");
        if (HEADLESS) opts.addArguments("--headless=new");
        return opts;
    }

    private static MutableCapabilities buildBrowserStackCapabilities() {
        MutableCapabilities caps = new MutableCapabilities();
        HashMap<String, Object> bsOptions = new HashMap<>();

        bsOptions.put("projectName",  "Amazon Automation Framework");
        bsOptions.put("buildName",    "Build-" + System.getenv("BUILD_NUMBER"));
        bsOptions.put("sessionName",  "Amazon-" + BROWSER);
        bsOptions.put("video",        true);
        bsOptions.put("networkLogs",  true);
        bsOptions.put("consoleLogs",  "info");

        switch (BROWSER.toLowerCase()) {
            case "safari" -> {
                caps.setCapability("browserName",    "Safari");
                bsOptions.put("os",             "OS X");
                bsOptions.put("osVersion",      "Ventura");
                bsOptions.put("browserVersion", "16.0");
            }
            case "ios" -> {
                caps.setCapability("deviceName",    "iPhone 15");
                caps.setCapability("platformName",  "iOS");
                caps.setCapability("platformVersion","17");
                caps.setCapability("browserName",   "Safari");
                bsOptions.put("realMobile", "true");
            }
            default -> {
                caps.setCapability("browserName",    "Chrome");
                bsOptions.put("os",             "Windows");
                bsOptions.put("osVersion",      "11");
                bsOptions.put("browserVersion", "latest");
            }
        }

        caps.setCapability("bstack:options", bsOptions);
        return caps;
    }
}
