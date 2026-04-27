package com.amazon.login.pages;

import com.amazon.framework.base.BasePage;
import com.amazon.framework.listeners.ExtentReportManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * LoginPage — owned by the Auth team.
 *
 * CODEOWNERS: /src/test/java/com/amazon/login/ @auth-team
 * No other team should raise PRs touching this package.
 */
public class LoginPage extends BasePage {

    // ─── Locators ─────────────────────────────────────────────────────────────
    @FindBy(id = "ap_email")
    private WebElement emailField;

    @FindBy(id = "continue")
    private WebElement continueButton;

    @FindBy(id = "ap_password")
    private WebElement passwordField;

    @FindBy(id = "signInSubmit")
    private WebElement signInButton;

    @FindBy(id = "auth-error-message-box")
    private WebElement errorMessage;

    @FindBy(id = "nav-link-accountList")
    private WebElement accountListLink;

    @FindBy(css = "#nav-link-accountList .nav-line-1")
    private WebElement accountGreeting;

    @FindBy(id = "ap_password_check")
    private WebElement showPasswordCheckbox;

    @FindBy(css = "#forgotPasswordLink a")
    private WebElement forgotPasswordLink;

    @FindBy(id = "createAccountSubmit")
    private WebElement createAccountButton;

    // ─── Page Actions ─────────────────────────────────────────────────────────
    public LoginPage enterEmail(String email) {
        ExtentReportManager.logStep("Entering email: " + email);
        type(emailField, email);
        return this;
    }

    public LoginPage clickContinue() {
        ExtentReportManager.logStep("Clicking Continue");
        click(continueButton);
        return this;
    }

    public LoginPage enterPassword(String password) {
        ExtentReportManager.logStep("Entering password");
        type(passwordField, password);
        return this;
    }

    public LoginPage clickSignIn() {
        ExtentReportManager.logStep("Clicking Sign In");
        click(signInButton);
        return this;
    }

    /**
     * Complete login flow in one call.
     * Chains: email → continue → password → sign in
     */
    public void login(String email, String password) {
        ExtentReportManager.logStep("Logging in as: " + email);
        enterEmail(email);
        clickContinue();
        enterPassword(password);
        clickSignIn();
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getAccountGreeting() {
        return getText(accountGreeting);
    }

    public boolean isLoggedIn() {
        try {
            String greeting = getAccountGreeting();
            return greeting != null && !greeting.toLowerCase().contains("hello, sign in");
        } catch (Exception e) {
            return false;
        }
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink);
    }

    // ─── Contract ─────────────────────────────────────────────────────────────
    @Override
    public boolean isLoaded() {
        return isDisplayed(emailField) || isDisplayed(passwordField);
    }
}
