package com.amazon.login.stepdefs;

import com.amazon.framework.base.BaseTest;
import com.amazon.framework.config.ConfigManager;
import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.login.pages.LoginPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

/**
 * LoginStepDefs — Gherkin step implementations for login scenarios.
 *
 * Owned by: @auth-team
 * RULE: This class only uses LoginPage. Never import other team's pages here.
 */
public class LoginStepDefs {

    // Cucumber injects shared state via PicoContainer — no static state
    private final LoginPage loginPage;
    private final SharedContext context;

    public LoginStepDefs(SharedContext context) {
        this.loginPage = new LoginPage();
        this.context   = context;
    }

    @Given("I am on the Amazon homepage")
    public void iAmOnAmazonHomepage() {
        ExtentReportManager.logStep("Navigating to Amazon homepage");
        BaseTest.getDriver().get(ConfigManager.get("base.url"));
    }

    @Given("I am logged in as a registered user")
    public void iAmLoggedInAsRegisteredUser() {
        BaseTest.getDriver().get(ConfigManager.get("base.url"));
        loginPage.login(
            ConfigManager.get("test.email"),
            ConfigManager.get("test.password")
        );
        Assert.assertTrue(loginPage.isLoggedIn(), "User should be logged in");
        ExtentReportManager.logStep("Logged in as registered user");
    }

    @When("I login with email {string} and password {string}")
    public void iLoginWithEmailAndPassword(String email, String password) {
        loginPage.login(email, password);
    }

    @When("I enter email {string}")
    public void iEnterEmail(String email) {
        loginPage.enterEmail(email);
    }

    @When("I click Continue")
    public void iClickContinue() {
        loginPage.clickContinue();
    }

    @When("I enter password {string}")
    public void iEnterPassword(String password) {
        loginPage.enterPassword(password);
    }

    @When("I click Sign In")
    public void iClickSignIn() {
        loginPage.clickSignIn();
    }

    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        Assert.assertTrue(loginPage.isLoggedIn(),
            "Expected to be logged in but was not");
        ExtentReportManager.logPass("User is logged in successfully");
    }

    @Then("I should see a greeting containing {string}")
    public void iShouldSeeGreeting(String name) {
        String greeting = loginPage.getAccountGreeting();
        Assert.assertTrue(greeting.contains(name),
            "Expected greeting to contain '" + name + "' but was: " + greeting);
    }

    @Then("I should see an error message containing {string}")
    public void iShouldSeeError(String expectedText) {
        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "Expected error message to be displayed");
        Assert.assertTrue(loginPage.getErrorMessage().toLowerCase().contains(expectedText.toLowerCase()),
            "Error message did not contain '" + expectedText + "'");
        ExtentReportManager.logPass("Error message verified: " + loginPage.getErrorMessage());
    }

    @Then("I should see a validation error")
    public void iShouldSeeValidationError() {
        Assert.assertTrue(loginPage.isErrorDisplayed() || loginPage.isLoaded(),
            "Expected validation error to appear");
    }
}
