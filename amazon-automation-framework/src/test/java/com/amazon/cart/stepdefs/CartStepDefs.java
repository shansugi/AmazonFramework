package com.amazon.cart.stepdefs;

import com.amazon.cart.pages.CartPage;
import com.amazon.framework.config.ConfigManager;
import com.amazon.framework.base.BaseTest;
import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.login.stepdefs.SharedContext;
import com.amazon.product.pages.ProductPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

/**
 * CartStepDefs — Shopping cart Gherkin step implementations.
 * Owned by: @cart-team
 * CODEOWNERS: /src/test/java/com/amazon/cart/ @cart-team
 */
public class CartStepDefs {

    private final CartPage cartPage;
    private final SharedContext context;

    public CartStepDefs(SharedContext context) {
        this.cartPage = new CartPage();
        this.context  = context;
    }

    @Given("I have added {string} to my cart")
    public void iHaveAddedProductToCart(String productKeyword) {
        ExtentReportManager.logStep("Adding '" + productKeyword + "' to cart via search");
        ProductPage productPage = new ProductPage();
        productPage.searchFor(productKeyword);
        productPage.clickFirstProduct();
        productPage.addToCart();
        context.initialCartCount = productPage.getCartCount();
        ExtentReportManager.logPass("Product added to cart. Cart count: " + context.initialCartCount);
    }

    @Given("I have a product in my cart")
    public void iHaveAProductInMyCart() {
        iHaveAddedProductToCart("USB cable");
    }

    @When("I navigate to the cart")
    public void iNavigateToTheCart() {
        ExtentReportManager.logStep("Navigating to cart");
        BaseTest.getDriver().get(ConfigManager.get("base.url") + "/gp/cart/view.html");
    }

    @Then("the cart should contain {int} item")
    public void cartShouldContainItems(int expectedCount) {
        int actual = cartPage.getCartItemCount();
        Assert.assertEquals(actual, expectedCount,
            "Expected " + expectedCount + " cart items but found " + actual);
        ExtentReportManager.logPass("Cart contains " + actual + " item(s) as expected");
    }

    @Then("the cart subtotal should be displayed")
    public void cartSubtotalShouldBeDisplayed() {
        String subtotal = cartPage.getSubtotal();
        Assert.assertNotNull(subtotal, "Cart subtotal should be displayed");
        Assert.assertFalse(subtotal.isEmpty(), "Subtotal should not be empty");
        context.cartTotalBeforeAction = subtotal;
        ExtentReportManager.logPass("Cart subtotal: " + subtotal);
    }

    @When("I update the quantity of item {int} to {string}")
    public void iUpdateQuantityOfItem(int itemIndex, String quantity) {
        ExtentReportManager.logStep("Updating item " + itemIndex + " quantity to " + quantity);
        context.cartTotalBeforeAction = cartPage.getSubtotal();
        cartPage.updateQuantity(itemIndex - 1, quantity);  // 1-based in Gherkin, 0-based in code
    }

    @Then("the cart subtotal should update accordingly")
    public void cartSubtotalShouldUpdate() {
        String newSubtotal = cartPage.getSubtotal();
        Assert.assertNotNull(newSubtotal, "Subtotal should be present after quantity update");
        Assert.assertNotEquals(newSubtotal, context.cartTotalBeforeAction,
            "Subtotal should have changed after quantity update");
        ExtentReportManager.logPass("Subtotal updated from " +
            context.cartTotalBeforeAction + " to " + newSubtotal);
    }

    @When("I delete item at index {int}")
    public void iDeleteItemAtIndex(int index) {
        ExtentReportManager.logStep("Deleting cart item at index: " + index);
        context.initialCartCount = cartPage.getCartItemCount();
        cartPage.deleteItem(index);
    }

    @Then("the cart should be empty")
    public void cartShouldBeEmpty() {
        Assert.assertTrue(cartPage.isCartEmpty(),
            "Expected cart to be empty but it still has items");
        ExtentReportManager.logPass("Cart is empty as expected");
    }

    @When("I save item {int} for later")
    public void iSaveItemForLater(int itemIndex) {
        ExtentReportManager.logStep("Saving item " + itemIndex + " for later");
        context.initialCartCount = cartPage.getCartItemCount();
        cartPage.saveItemForLater(itemIndex);
    }

    @Then("the cart items count should decrease")
    public void cartItemsCountShouldDecrease() {
        int newCount = cartPage.getCartItemCount();
        Assert.assertTrue(newCount < context.initialCartCount,
            "Cart count should have decreased. Was: " +
            context.initialCartCount + ", now: " + newCount);
        ExtentReportManager.logPass("Cart item count decreased from " +
            context.initialCartCount + " to " + newCount);
    }

    @Then("saved for later count should increase")
    public void savedForLaterCountShouldIncrease() {
        int savedCount = cartPage.getSavedItemCount();
        Assert.assertTrue(savedCount > 0, "Expected at least 1 saved-for-later item");
        ExtentReportManager.logPass("Saved for later count: " + savedCount);
    }

    @When("I apply coupon code {string}")
    public void iApplyCouponCode(String code) {
        ExtentReportManager.logStep("Applying coupon: " + code);
        cartPage.applyCoupon(code);
    }

    @Then("the coupon should be applied successfully")
    public void couponShouldBeApplied() {
        Assert.assertTrue(cartPage.isCouponApplied(),
            "Expected coupon to be applied successfully");
        ExtentReportManager.logPass("Coupon applied successfully");
    }

    @Then("I should see a coupon error message")
    public void iShouldSeeCouponError() {
        String error = cartPage.getCouponError();
        Assert.assertNotNull(error, "Expected a coupon error message");
        Assert.assertFalse(error.isEmpty(), "Coupon error message should not be empty");
        ExtentReportManager.logPass("Coupon error shown: " + error);
    }

    @When("I click Proceed to Checkout")
    public void iClickProceedToCheckout() {
        ExtentReportManager.logStep("Clicking Proceed to Checkout");
        cartPage.proceedToCheckout();
    }

    @Then("I should be on the checkout page")
    public void iShouldBeOnCheckoutPage() {
        Assert.assertTrue(
            BaseTest.getDriver().getCurrentUrl().contains("checkout"),
            "Expected to be on checkout page but URL was: " +
            BaseTest.getDriver().getCurrentUrl());
        ExtentReportManager.logPass("On checkout page");
    }
}
