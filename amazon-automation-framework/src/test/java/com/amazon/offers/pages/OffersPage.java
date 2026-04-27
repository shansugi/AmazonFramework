package com.amazon.offers.pages;

import com.amazon.framework.base.BasePage;
import com.amazon.framework.listeners.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OffersPage — Today's Deals, Lightning Deals, Coupons.
 * Owned by: @offers-team
 * CODEOWNERS: /src/test/java/com/amazon/offers/ @offers-team
 */
public class OffersPage extends BasePage {

    @FindBy(css = ".dealCard")
    private List<WebElement> dealCards;

    @FindBy(css = ".dealCard .a-size-base-plus")
    private List<WebElement> dealTitles;

    @FindBy(css = ".dealCard .a-color-price")
    private List<WebElement> dealPrices;

    @FindBy(css = ".dealCard .dealBadge")
    private List<WebElement> dealBadges;

    @FindBy(css = ".a-badge-text")
    private List<WebElement> badgeTexts;

    @FindBy(id = "widgetFilters")
    private WebElement filterPanel;

    @FindBy(css = ".a-button-text.filter-text")
    private List<WebElement> categoryFilters;

    @FindBy(css = ".lightning-deal-timer")
    private List<WebElement> lightningDealTimers;

    @FindBy(css = ".deal-progress-bar")
    private List<WebElement> dealProgressBars;

    @FindBy(css = ".clipButton")
    private List<WebElement> couponClipButtons;

    @FindBy(css = ".couponApplied")
    private List<WebElement> appliedCoupons;

    // ─── Actions ──────────────────────────────────────────────────────────────
    public int getDealCount() {
        return dealCards.size();
    }

    public List<String> getAllDealTitles() {
        return dealTitles.stream().map(this::getText).collect(Collectors.toList());
    }

    public List<String> getAllDealPrices() {
        return dealPrices.stream().map(this::getText).collect(Collectors.toList());
    }

    public void filterByCategory(String category) {
        ExtentReportManager.logStep("Filtering deals by: " + category);
        categoryFilters.stream()
                .filter(f -> getText(f).equalsIgnoreCase(category))
                .findFirst()
                .ifPresent(this::click);
    }

    public void clipCoupon(int index) {
        ExtentReportManager.logStep("Clipping coupon at index: " + index);
        scrollToElement(couponClipButtons.get(index));
        click(couponClipButtons.get(index));
    }

    public int getClippedCouponCount() {
        return appliedCoupons.size();
    }

    public boolean hasLightningDeals() {
        return !lightningDealTimers.isEmpty();
    }

    public String getLightningDealTimer(int index) {
        return getText(lightningDealTimers.get(index));
    }

    public void clickDeal(int index) {
        ExtentReportManager.logStep("Clicking deal at index: " + index);
        click(dealCards.get(index));
    }

    public List<String> getDealsWithBadge(String badge) {
        return dealBadges.stream()
                .filter(b -> getText(b).equalsIgnoreCase(badge))
                .map(b -> getText(b.findElement(By.xpath("../../.."))))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isLoaded() {
        return getCurrentUrl().contains("deals") && !dealCards.isEmpty();
    }
}
