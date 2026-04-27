package com.amazon.framework.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * BasePage — the ENFORCED CONTRACT for all page classes.
 *
 * RULES (enforced in code review):
 *  1. All page classes MUST extend BasePage
 *  2. No raw driver.findElement() in test classes — use page methods
 *  3. No Thread.sleep() — use waitFor* methods below
 *  4. All Selenium interactions go through protected methods here
 *  5. Implement isLoaded() to verify page state
 */
public abstract class BasePage {

    private static final Logger log = LogManager.getLogger(BasePage.class);
    private static final int DEFAULT_TIMEOUT = 15;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;

    public BasePage() {
        this.driver  = BaseTest.getDriver();
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTRACT — every page MUST implement this
    // ─────────────────────────────────────────────────────────────────────────
    public abstract boolean isLoaded();

    // ─────────────────────────────────────────────────────────────────────────
    // CLICK ACTIONS
    // ─────────────────────────────────────────────────────────────────────────
    protected void click(WebElement element) {
        waitForClickable(element);
        highlight(element);
        element.click();
        log.debug("Clicked: {}", getElementDescription(element));
    }

    protected void clickWithJS(WebElement element) {
        waitForVisible(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        log.debug("JS Clicked: {}", getElementDescription(element));
    }

    protected void clickByLocator(By locator) {
        click(wait.until(ExpectedConditions.elementToBeClickable(locator)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INPUT ACTIONS
    // ─────────────────────────────────────────────────────────────────────────
    protected void type(WebElement element, String text) {
        waitForVisible(element);
        element.clear();
        element.sendKeys(text);
        log.debug("Typed '{}' into: {}", text, getElementDescription(element));
    }

    protected void typeSlowly(WebElement element, String text) {
        waitForVisible(element);
        element.clear();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
        }
    }

    protected void clearAndType(WebElement element, String text) {
        waitForVisible(element);
        element.sendKeys(Keys.CONTROL + "a");
        element.sendKeys(Keys.DELETE);
        element.sendKeys(text);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ ACTIONS
    // ─────────────────────────────────────────────────────────────────────────
    protected String getText(WebElement element) {
        waitForVisible(element);
        return element.getText().trim();
    }

    protected String getAttribute(WebElement element, String attr) {
        waitForVisible(element);
        return element.getAttribute(attr);
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DROPDOWN
    // ─────────────────────────────────────────────────────────────────────────
    protected void selectByVisibleText(WebElement element, String text) {
        waitForVisible(element);
        new Select(element).selectByVisibleText(text);
        log.debug("Selected '{}' from dropdown", text);
    }

    protected void selectByValue(WebElement element, String value) {
        waitForVisible(element);
        new Select(element).selectByValue(value);
    }

    protected String getSelectedOption(WebElement element) {
        waitForVisible(element);
        return new Select(element).getFirstSelectedOption().getText();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WAIT METHODS — use these instead of Thread.sleep()
    // ─────────────────────────────────────────────────────────────────────────
    protected WebElement waitForVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitForClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected boolean waitForInvisible(WebElement element) {
        return wait.until(ExpectedConditions.invisibilityOf(element));
    }

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> waitForElements(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected boolean waitForText(WebElement element, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    protected boolean waitForUrl(String urlFragment) {
        return wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    protected WebElement waitForVisibleWithTimeout(By locator, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCROLL
    // ─────────────────────────────────────────────────────────────────────────
    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    protected void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0);");
    }

    protected void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,document.body.scrollHeight);");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HOVER
    // ─────────────────────────────────────────────────────────────────────────
    protected void hoverOver(WebElement element) {
        waitForVisible(element);
        actions.moveToElement(element).perform();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────────
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected void switchToNewTab() {
        String newTab = driver.getWindowHandles().stream()
                .filter(h -> !h.equals(driver.getWindowHandle()))
                .findFirst().orElseThrow();
        driver.switchTo().window(newTab);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JAVASCRIPT
    // ─────────────────────────────────────────────────────────────────────────
    protected Object executeJS(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DEBUG HELPER — highlights element briefly (useful during dev)
    // ─────────────────────────────────────────────────────────────────────────
    private void highlight(WebElement element) {
        if (Boolean.parseBoolean(System.getProperty("highlight", "false"))) {
            try {
                executeJS("arguments[0].style.border='3px solid red'", element);
            } catch (Exception ignored) {}
        }
    }

    private String getElementDescription(WebElement element) {
        try {
            return element.toString().replaceAll(".*->", "").replace("]", "").trim();
        } catch (Exception e) {
            return "unknown element";
        }
    }
}
