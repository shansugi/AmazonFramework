package com.amazon.checkout.tests;

import com.amazon.cart.pages.CartPage;
import com.amazon.checkout.pages.CheckoutPage;
import com.amazon.framework.base.BaseTest;
import com.amazon.framework.config.ConfigManager;
import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.framework.listeners.RetryAnalyzer;
import com.amazon.framework.utils.ExcelUtils;
import com.amazon.login.pages.LoginPage;
import com.amazon.product.pages.ProductPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * OrderFlowTest — Full end-to-end order flow tests.
 * Owned by: @checkout-team
 *
 * Demonstrates:
 * 1. ThreadLocal WebDriver (via BaseTest)
 * 2. Parallel execution (parallel="methods" in testng.xml, threadPoolSize here)
 * 3. Excel DataProvider for data-driven testing
 * 4. Full flow: login → search → add to cart → checkout → confirm
 * 5. RetryAnalyzer for flaky test resilience
 */
public class OrderFlowTest extends BaseTest {

    // ─────────────────────────────────────────────────────────────────────────
    // DATA PROVIDERS — from Excel (data-driven)
    // ─────────────────────────────────────────────────────────────────────────
    @DataProvider(name = "orderData", parallel = true)
    public Object[][] orderData() {
        // Each row = one parallel test execution
        return ExcelUtils.getSheetData(
            "src/test/resources/testdata/orders.xlsx", "OrderData");
    }

    @DataProvider(name = "searchProducts", parallel = true)
    public Object[][] searchProducts() {
        return ExcelUtils.getSheetData(
            "src/test/resources/testdata/products.xlsx", "SearchData");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Full E2E order flow from Excel data, parallel
    // ─────────────────────────────────────────────────────────────────────────
    @Test(dataProvider    = "orderData",
          retryAnalyzer   = RetryAnalyzer.class,
          groups          = {"checkout", "regression", "p1"},
          description     = "E2E order flow driven by Excel data",
          threadPoolSize  = 3)
    public void testEndToEndOrderFlow(
            String email, String password, String searchProduct,
            String deliveryName, String phone, String address,
            String city, String state, String pincode) {

        ExtentReportManager.logStep("Starting E2E order flow for: " + searchProduct);

        // Step 1: Navigate and Login
        navigateToAmazon();
        LoginPage loginPage = new LoginPage();
        loginPage.login(email, password);
        Assert.assertTrue(loginPage.isLoggedIn(),
            "Login failed for: " + email);
        ExtentReportManager.logPass("Logged in as: " + email);

        // Step 2: Search for product
        ProductPage productPage = new ProductPage();
        productPage.searchFor(searchProduct);
        Assert.assertTrue(productPage.getSearchResultCount() > 0,
            "No results found for: " + searchProduct);
        ExtentReportManager.logStep("Found results for: " + searchProduct);

        // Step 3: Click first product and verify PDP
        productPage.clickFirstProduct();
        String productTitle = productPage.getProductTitle();
        String productPrice = productPage.getProductPrice();
        Assert.assertNotNull(productTitle, "Product title missing on PDP");
        ExtentReportManager.logPass("Product: " + productTitle + " | Price: " + productPrice);

        // Step 4: Add to cart
        productPage.addToCart();
        ExtentReportManager.logStep("Added to cart: " + productTitle);

        // Step 5: Go to cart and verify
        getDriver().get(ConfigManager.get("base.url") + "/cart");
        CartPage cartPage = new CartPage();
        Assert.assertTrue(cartPage.getCartItemCount() > 0, "Cart is empty after adding product");
        String cartSubtotal = cartPage.getSubtotal();
        ExtentReportManager.logPass("Cart verified. Subtotal: " + cartSubtotal);

        // Step 6: Proceed to checkout
        cartPage.proceedToCheckout();
        CheckoutPage checkoutPage = new CheckoutPage();

        // Step 7: Fill shipping address
        checkoutPage.fillShippingAddress(deliveryName, phone, address, city, state, pincode);
        ExtentReportManager.logStep("Shipping address filled for: " + deliveryName);

        // Step 8: Select payment (COD for test safety)
        checkoutPage.selectCashOnDelivery();
        ExtentReportManager.logStep("Payment method: Cash on Delivery");

        // Step 9: Verify order total
        String orderTotal = checkoutPage.getOrderTotal();
        Assert.assertNotNull(orderTotal, "Order total not displayed on review page");
        ExtentReportManager.logPass("Order total on review: " + orderTotal);

        // Step 10: Place order
        checkoutPage.placeOrder();
        Assert.assertTrue(checkoutPage.isOrderPlaced(),
            "Order confirmation not shown after placing order");

        String orderId = checkoutPage.getOrderId();
        ExtentReportManager.logPass("Order placed! Order ID: " + orderId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Parallel search + add to cart (smoke)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(dataProvider   = "searchProducts",
          retryAnalyzer  = RetryAnalyzer.class,
          groups         = {"product", "smoke", "p1"},
          description    = "Search and add products to cart in parallel",
          threadPoolSize = 5)
    public void testSearchAndAddToCart(String keyword, String minResults) {
        ExtentReportManager.logStep("Testing product search: " + keyword);

        navigateToAmazon();
        ProductPage productPage = new ProductPage();
        productPage.searchFor(keyword);

        int resultCount = productPage.getSearchResultCount();
        Assert.assertTrue(resultCount >= Integer.parseInt(minResults),
            "Expected at least " + minResults + " results for '" + keyword +
            "' but got " + resultCount);
        ExtentReportManager.logPass("Search for '" + keyword + "' returned " + resultCount + " results");

        // Click first product and add to cart
        productPage.clickFirstProduct();
        String title = productPage.getProductTitle();
        ExtentReportManager.logStep("Product: " + title);

        if (productPage.isInStock()) {
            int cartBefore = productPage.getCartCount();
            productPage.addToCart();
            int cartAfter = productPage.getCartCount();
            Assert.assertEquals(cartAfter, cartBefore + 1, "Cart count did not increase");
            ExtentReportManager.logPass("Added to cart. Count: " + cartBefore + " → " + cartAfter);
        } else {
            ExtentReportManager.logStep("Product out of stock — skipping add to cart");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Cart manipulation
    // ─────────────────────────────────────────────────────────────────────────
    @Test(retryAnalyzer = RetryAnalyzer.class,
          groups        = {"cart", "regression", "p2"},
          description   = "Verify cart update and removal operations")
    public void testCartOperations() {
        navigateToAmazon();

        LoginPage loginPage = new LoginPage();
        loginPage.login(ConfigManager.get("test.email"), ConfigManager.get("test.password"));
        Assert.assertTrue(loginPage.isLoggedIn());

        // Add a product
        ProductPage productPage = new ProductPage();
        productPage.searchFor("USB hub");
        productPage.clickFirstProduct();
        productPage.addToCart();

        // Navigate to cart
        getDriver().get(ConfigManager.get("base.url") + "/cart");
        CartPage cartPage = new CartPage();
        int initialCount = cartPage.getCartItemCount();
        ExtentReportManager.logStep("Cart item count: " + initialCount);

        // Update quantity
        if (initialCount > 0) {
            cartPage.updateQuantity(0, "2");
            String newSubtotal = cartPage.getSubtotal();
            Assert.assertNotNull(newSubtotal, "Subtotal should update after quantity change");
            ExtentReportManager.logPass("Quantity updated. New subtotal: " + newSubtotal);

            // Delete item
            cartPage.deleteItem(0);
            ExtentReportManager.logPass("Item deleted from cart");
        }
    }
}
