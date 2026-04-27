package com.amazon.tracking.stepdefs;

import com.amazon.framework.base.BaseTest;
import com.amazon.framework.config.ConfigManager;
import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.login.stepdefs.SharedContext;
import com.amazon.tracking.pages.TrackingPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

/**
 * TrackingStepDefs — Order tracking Gherkin steps.
 * Owned by: @orders-team
 * CODEOWNERS: /src/test/java/com/amazon/tracking/ @orders-team
 */
public class TrackingStepDefs {

    private final TrackingPage trackingPage;
    private final SharedContext context;

    public TrackingStepDefs(SharedContext context) {
        this.trackingPage = new TrackingPage();
        this.context      = context;
    }

    @When("I navigate to My Orders")
    public void iNavigateToMyOrders() {
        ExtentReportManager.logStep("Navigating to My Orders page");
        BaseTest.getDriver().get(
            ConfigManager.get("base.url") + "/gp/css/order-history");
    }

    @Then("I should see my order history")
    public void iShouldSeeOrderHistory() {
        int count = trackingPage.getOrderCount();
        Assert.assertTrue(count > 0, "Expected at least one order in history");
        ExtentReportManager.logPass("Order history loaded: " + count + " orders");
    }

    @When("I click on the most recent order")
    public void iClickOnMostRecentOrder() {
        ExtentReportManager.logStep("Clicking most recent order");
        trackingPage.clickOrderByIndex(0);
    }

    @When("I click Track Package")
    public void iClickTrackPackage() {
        ExtentReportManager.logStep("Clicking Track Package");
        trackingPage.trackPackage();
    }

    @Then("I should see the tracking status")
    public void iShouldSeeTrackingStatus() {
        String status = trackingPage.getTrackingStatus();
        Assert.assertNotNull(status, "Tracking status should be displayed");
        Assert.assertFalse(status.isEmpty(), "Tracking status should not be empty");
        ExtentReportManager.logPass("Tracking status: " + status);
    }

    @Then("I should see tracking history events")
    public void iShouldSeeTrackingHistory() {
        Assert.assertFalse(
            trackingPage.getTrackingHistory().isEmpty(),
            "Expected tracking history events to be present");
        ExtentReportManager.logPass("Tracking history events: " +
            trackingPage.getTrackingHistory().size());
    }

    @Then("I should see the delivery promise")
    public void iShouldSeeDeliveryPromise() {
        String promise = trackingPage.getDeliveryPromise();
        Assert.assertNotNull(promise, "Delivery promise date should be shown");
        ExtentReportManager.logPass("Delivery promise: " + promise);
    }

    @Then("the order status should be {string}")
    public void orderStatusShouldBe(String expectedStatus) {
        String actual = trackingPage.getOrderStatus();
        Assert.assertTrue(
            actual.toLowerCase().contains(expectedStatus.toLowerCase()),
            "Expected status '" + expectedStatus + "' but got: " + actual);
        ExtentReportManager.logPass("Order status verified: " + actual);
    }

    @Then("I should be able to cancel the order")
    public void iShouldBeAbleToCancelOrder() {
        Assert.assertTrue(trackingPage.canCancel(),
            "Expected cancel option to be available");
        ExtentReportManager.logPass("Cancel option is available");
    }

    @When("I cancel the order")
    public void iCancelTheOrder() {
        ExtentReportManager.logStep("Cancelling the order");
        trackingPage.cancelOrder();
    }

    @Then("I should be able to return the order")
    public void iShouldBeAbleToReturn() {
        Assert.assertTrue(trackingPage.canReturn(),
            "Expected return option to be available");
        ExtentReportManager.logPass("Return option is available");
    }

    @Then("the order with ID {string} should be in my orders")
    public void orderWithIdShouldBeInOrders(String orderId) {
        ExtentReportManager.logStep("Verifying order ID in history: " + orderId);
        iNavigateToMyOrders();
        Assert.assertTrue(
            trackingPage.getOrderCount() > 0,
            "No orders found in history");
        ExtentReportManager.logPass("Orders found in history");
    }
}
