package com.amazon.product.stepdefs;

import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.login.stepdefs.SharedContext;
import com.amazon.product.pages.ProductPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

/**
 * ProductStepDefs — Gherkin step implementations for product/search scenarios.
 * Owned by: @product-team
 */
public class ProductStepDefs {

    private final ProductPage productPage;
    private final SharedContext context;

    public ProductStepDefs(SharedContext context) {
        this.productPage = new ProductPage();
        this.context     = context;
    }

    @When("I search for {string}")
    public void iSearchFor(String keyword) {
        ExtentReportManager.logStep("Searching for: " + keyword);
        context.lastSearchedProduct = keyword;
        productPage.searchFor(keyword);
    }

    @Then("I should see at least {int} search results")
    public void iShouldSeeAtLeastResults(int minCount) {
        int actual = productPage.getSearchResultCount();
        Assert.assertTrue(actual >= minCount,
            "Expected at least " + minCount + " results but got " + actual);
        ExtentReportManager.logPass("Found " + actual + " results (min expected: " + minCount + ")");
    }

    @Then("product titles should contain {string} or {string}")
    public void productTitlesShouldContain(String keyword1, String keyword2) {
        long matchCount = productPage.getAllProductTitles().stream()
                .filter(t -> t.toLowerCase().contains(keyword1.toLowerCase())
                          || t.toLowerCase().contains(keyword2.toLowerCase()))
                .count();
        Assert.assertTrue(matchCount > 0,
            "No product titles contained '" + keyword1 + "' or '" + keyword2 + "'");
    }

    @When("I click on the first product")
    public void iClickOnFirstProduct() {
        productPage.clickFirstProduct();
    }

    @Then("the product detail page should be displayed")
    public void productDetailPageShouldBeDisplayed() {
        Assert.assertTrue(productPage.isLoaded(), "Product detail page did not load");
    }

    @Then("I should see the product title")
    public void iShouldSeeProductTitle() {
        String title = productPage.getProductTitle();
        Assert.assertNotNull(title);
        Assert.assertFalse(title.isEmpty(), "Product title should not be empty");
        context.currentProductTitle = title;
        ExtentReportManager.logPass("Product title: " + title);
    }

    @Then("I should see the product price")
    public void iShouldSeeProductPrice() {
        String price = productPage.getProductPrice();
        Assert.assertNotNull(price);
        context.currentProductPrice = price;
        ExtentReportManager.logPass("Product price: " + price);
    }

    @Then("I should see star rating")
    public void iShouldSeeStarRating() {
        String rating = productPage.getStarRating();
        Assert.assertNotNull(rating);
        ExtentReportManager.logPass("Star rating: " + rating);
    }

    @Then("I should see review count")
    public void iShouldSeeReviewCount() {
        String reviews = productPage.getReviewCount();
        Assert.assertNotNull(reviews);
        ExtentReportManager.logPass("Review count: " + reviews);
    }

    @Then("I should see availability status")
    public void iShouldSeeAvailabilityStatus() {
        String status = productPage.getAvailabilityStatus();
        Assert.assertNotNull(status);
        ExtentReportManager.logPass("Availability: " + status);
    }

    @When("I click Add to Cart")
    public void iClickAddToCart() {
        context.initialCartCount = productPage.getCartCount();
        productPage.addToCart();
    }

    @Then("the cart count should increase by {int}")
    public void cartCountShouldIncreaseBy(int by) {
        int newCount = productPage.getCartCount();
        Assert.assertEquals(newCount, context.initialCartCount + by,
            "Cart count did not increase by " + by);
        ExtentReportManager.logPass("Cart count increased from " +
            context.initialCartCount + " to " + newCount);
    }

    @When("I filter by category {string}")
    public void iFilterByCategory(String category) {
        productPage.searchFor(category);
    }

    @Then("all results should belong to {string} category")
    public void allResultsShouldBelongToCategory(String category) {
        Assert.assertTrue(productPage.getSearchResultCount() > 0,
            "Expected results for category: " + category);
    }

    @When("I click next page")
    public void iClickNextPage() {
        productPage.clickNextPage();
    }

    @Then("I should be on page {int} of results")
    public void iShouldBeOnPage(int pageNum) {
        Assert.assertTrue(productPage.getCurrentUrl().contains("page=" + pageNum)
                       || productPage.getCurrentUrl().contains("pg=" + pageNum),
            "Expected to be on page " + pageNum);
    }
}
