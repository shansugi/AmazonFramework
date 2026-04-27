package com.amazon.offers.stepdefs;

import com.amazon.framework.base.BaseTest;
import com.amazon.framework.config.ConfigManager;
import com.amazon.framework.listeners.ExtentReportManager;
import com.amazon.login.stepdefs.SharedContext;
import com.amazon.offers.pages.OffersPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

/**
 * OffersStepDefs — Today's Deals / Offers page Gherkin steps.
 * Owned by: @offers-team
 * CODEOWNERS: /src/test/java/com/amazon/offers/ @offers-team
 */
public class OffersStepDefs {

    private final OffersPage offersPage;
    private final SharedContext context;

    public OffersStepDefs(SharedContext context) {
        this.offersPage = new OffersPage();
        this.context    = context;
    }

    @When("I navigate to Today's Deals")
    public void iNavigateToTodaysDeals() {
        ExtentReportManager.logStep("Navigating to Today's Deals page");
        BaseTest.getDriver().get(ConfigManager.get("base.url") + "/deals");
    }

    @Then("I should see deal cards on the page")
    public void iShouldSeeDealCards() {
        int count = offersPage.getDealCount();
        Assert.assertTrue(count > 0, "Expected at least one deal card on the page");
        ExtentReportManager.logPass("Deal cards found: " + count);
    }

    @Then("each deal card should have a title and price")
    public void eachDealCardShouldHaveTitleAndPrice() {
        Assert.assertFalse(offersPage.getAllDealTitles().isEmpty(),
            "Deal titles should be present");
        Assert.assertFalse(offersPage.getAllDealPrices().isEmpty(),
            "Deal prices should be present");
        ExtentReportManager.logPass("All deal cards have titles and prices");
    }

    @When("I filter deals by {string}")
    public void iFilterDealsBy(String category) {
        ExtentReportManager.logStep("Filtering deals by category: " + category);
        offersPage.filterByCategory(category);
    }

    @Then("I should see deals for {string} category")
    public void iShouldSeeDealsForCategory(String category) {
        Assert.assertTrue(offersPage.getDealCount() > 0,
            "Expected deals in category: " + category);
        ExtentReportManager.logPass("Deals found for category: " + category);
    }

    @Then("I should see lightning deals with timers")
    public void iShouldSeeLightningDeals() {
        Assert.assertTrue(offersPage.hasLightningDeals(),
            "Expected lightning deals to be present on the page");
        String timer = offersPage.getLightningDealTimer(0);
        Assert.assertNotNull(timer, "Lightning deal timer should be displayed");
        ExtentReportManager.logPass("Lightning deal timer: " + timer);
    }

    @When("I clip a coupon at position {int}")
    public void iClipACoupon(int position) {
        ExtentReportManager.logStep("Clipping coupon at position: " + position);
        offersPage.clipCoupon(position - 1);  // 1-based in Gherkin, 0-based in code
    }

    @Then("the coupon should be clipped successfully")
    public void couponShouldBeClipped() {
        int clipped = offersPage.getClippedCouponCount();
        Assert.assertTrue(clipped > 0, "Expected at least one clipped coupon");
        ExtentReportManager.logPass("Clipped coupons: " + clipped);
    }

    @When("I click on deal at position {int}")
    public void iClickOnDealAtPosition(int position) {
        ExtentReportManager.logStep("Clicking deal at position: " + position);
        offersPage.clickDeal(position - 1);
    }

    @Then("I should be on a product page")
    public void iShouldBeOnProductPage() {
        String url = BaseTest.getDriver().getCurrentUrl();
        Assert.assertTrue(
            url.contains("/dp/") || url.contains("product"),
            "Expected to land on a product page but URL was: " + url);
        ExtentReportManager.logPass("Navigated to product page from deal");
    }
}
