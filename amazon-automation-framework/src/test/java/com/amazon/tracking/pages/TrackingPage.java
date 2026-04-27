package com.amazon.tracking.pages;

import com.amazon.framework.base.BasePage;
import com.amazon.framework.listeners.ExtentReportManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TrackingPage — Track My Order / Orders history page.
 * Owned by: @orders-team
 * CODEOWNERS: /src/test/java/com/amazon/tracking/ @orders-team
 */
public class TrackingPage extends BasePage {

    @FindBy(css = ".order-info .a-link-normal")
    private List<WebElement> orderLinks;

    @FindBy(css = ".order-date")
    private List<WebElement> orderDates;

    @FindBy(css = ".order-info .a-color-price")
    private List<WebElement> orderTotals;

    @FindBy(css = ".shipment-tracking-status")
    private WebElement trackingStatus;

    @FindBy(css = ".tracking-detail-list li")
    private List<WebElement> trackingHistory;

    @FindBy(css = ".delivery-promise")
    private WebElement deliveryPromise;

    @FindBy(css = ".order-status-label")
    private WebElement orderStatus;

    @FindBy(id = "orderId")
    private WebElement orderIdField;

    @FindBy(css = ".track-package-button")
    private WebElement trackPackageButton;

    @FindBy(css = ".cancel-item-button")
    private WebElement cancelOrderButton;

    @FindBy(css = ".return-items-button")
    private WebElement returnItemButton;

    // ─── Actions ──────────────────────────────────────────────────────────────
    public int getOrderCount() {
        return orderLinks.size();
    }

    public void clickOrderByIndex(int index) {
        ExtentReportManager.logStep("Opening order at index: " + index);
        click(orderLinks.get(index));
    }

    public void trackPackage() {
        ExtentReportManager.logStep("Clicking Track Package");
        click(trackPackageButton);
    }

    public String getTrackingStatus() {
        return getText(trackingStatus);
    }

    public List<String> getTrackingHistory() {
        return trackingHistory.stream().map(this::getText).collect(Collectors.toList());
    }

    public String getDeliveryPromise() {
        return getText(deliveryPromise);
    }

    public String getOrderStatus() {
        return getText(orderStatus);
    }

    public boolean canCancel() {
        return isDisplayed(cancelOrderButton);
    }

    public void cancelOrder() {
        ExtentReportManager.logStep("Cancelling order");
        click(cancelOrderButton);
    }

    public boolean canReturn() {
        return isDisplayed(returnItemButton);
    }

    @Override
    public boolean isLoaded() {
        return getCurrentUrl().contains("orders") || getCurrentUrl().contains("returns");
    }
}
