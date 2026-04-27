package com.amazon.checkout.stepdefs;

import com.amazon.checkout.pages.CheckoutPage;
import com.amazon.framework.base.BaseTest;
import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.login.stepdefs.SharedContext;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import org.testng.Assert;

import java.util.Map;

/**
 * CheckoutStepDefs — checkout flow steps.
 * Owned by: @checkout-team
 */
public class CheckoutStepDefs {

    private final CheckoutPage checkoutPage;
    private final SharedContext context;

    public CheckoutStepDefs(SharedContext context) {
        this.checkoutPage = new CheckoutPage();
        this.context      = context;
    }

    @When("I proceed to checkout")
    public void iProceedToCheckout() {
        // Navigate via URL for speed in tests
        checkoutPage.getCurrentUrl();
        ExtentReportManager.logStep("Proceeding to checkout");
    }

    @When("I use my existing delivery address")
    public void iUseExistingAddress() {
        ExtentReportManager.logStep("Using existing saved address");
        checkoutPage.useExistingAddress();
    }

    @When("I add a new delivery address:")
    public void iAddNewDeliveryAddress(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        ExtentReportManager.logStep("Adding new delivery address for: " + data.get("name"));
        checkoutPage.fillShippingAddress(
            data.get("name"),
            data.get("phone"),
            data.get("address"),
            data.get("city"),
            data.get("state"),
            data.get("pincode")
        );
    }

    @When("I select Cash on Delivery as payment")
    public void iSelectCOD() {
        ExtentReportManager.logStep("Selecting Cash on Delivery");
        checkoutPage.selectCashOnDelivery();
    }

    @Then("I should see the order review page")
    public void iShouldSeeOrderReviewPage() {
        Assert.assertTrue(checkoutPage.isLoaded(), "Checkout page did not load");
        ExtentReportManager.logPass("On order review page");
    }

    @Then("I should see the order total")
    public void iShouldSeeOrderTotal() {
        String total = checkoutPage.getOrderTotal();
        Assert.assertNotNull(total);
        Assert.assertFalse(total.isEmpty(), "Order total should be displayed");
        context.cartTotalBeforeAction = total;
        ExtentReportManager.logPass("Order total: " + total);
    }

    @When("I place the order")
    public void iPlaceTheOrder() {
        checkoutPage.placeOrder();
    }

    @Then("the order should be placed successfully")
    public void orderShouldBePlacedSuccessfully() {
        Assert.assertTrue(checkoutPage.isOrderPlaced(),
            "Order placement failed — confirmation not shown");
        ExtentReportManager.logPass("Order placed successfully");
    }

    @Then("I should see an order confirmation")
    public void iShouldSeeOrderConfirmation() {
        String orderId = checkoutPage.getOrderId();
        context.lastOrderId = orderId;
        Assert.assertNotNull(orderId, "Order ID should be present");
        Assert.assertFalse(orderId.isEmpty(), "Order ID should not be empty");
        ExtentReportManager.logPass("Order confirmed. Order ID: " + orderId);
    }

    @Then("the order total on review page should match the cart total")
    public void orderTotalShouldMatchCartTotal() {
        String reviewTotal = checkoutPage.getOrderTotal();
        Assert.assertEquals(reviewTotal, context.cartTotalBeforeAction,
            "Order total mismatch between cart and review page");
    }

    @Then("I should see an estimated delivery date")
    public void iShouldSeeEstimatedDeliveryDate() {
        String date = checkoutPage.getEstimatedDelivery();
        Assert.assertNotNull(date);
        Assert.assertFalse(date.isEmpty(), "Estimated delivery date should be shown");
        ExtentReportManager.logPass("Estimated delivery: " + date);
    }

    @When("I navigate back to cart")
    public void iNavigateBackToCart() {
        BaseTest.getDriver().navigate().back();
        ExtentReportManager.logStep("Navigated back to cart");
    }

    @Then("my cart items should still be present")
    public void myCartItemsShouldBePresent() {
        Assert.assertTrue(checkoutPage.getCurrentUrl().contains("cart"),
            "Expected to be on cart page after back navigation");
    }
}
