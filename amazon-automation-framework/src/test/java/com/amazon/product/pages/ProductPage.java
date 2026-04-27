package com.amazon.product.pages;

import com.amazon.framework.base.BasePage;
import com.amazon.framework.listeners.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductPage — Search results + Product Detail Page (PDP).
 * Owned by: @product-team
 * CODEOWNERS: /src/test/java/com/amazon/product/ @product-team
 */
public class ProductPage extends BasePage {

    // ─── Search Bar (present on all pages) ────────────────────────────────────
    @FindBy(id = "twotabsearchtextbox")
    private WebElement searchBox;

    @FindBy(id = "nav-search-submit-button")
    private WebElement searchButton;

    // ─── Search Results ───────────────────────────────────────────────────────
    @FindBy(css = "[data-component-type='s-search-result']")
    private List<WebElement> searchResults;

    @FindBy(css = ".s-result-item .a-price-whole")
    private List<WebElement> productPrices;

    @FindBy(css = ".s-result-item h2 a span")
    private List<WebElement> productTitles;

    @FindBy(css = ".s-result-item .a-link-normal.a-text-normal")
    private List<WebElement> productLinks;

    @FindBy(css = ".s-pagination-next")
    private WebElement nextPageButton;

    // ─── Product Detail Page (PDP) ────────────────────────────────────────────
    @FindBy(id = "productTitle")
    private WebElement productTitle;

    @FindBy(css = ".a-price .a-offscreen")
    private WebElement productPrice;

    @FindBy(id = "acrCustomerReviewText")
    private WebElement reviewCount;

    @FindBy(css = "#acrPopover .a-icon-alt")
    private WebElement starRating;

    @FindBy(id = "add-to-cart-button")
    private WebElement addToCartButton;

    @FindBy(id = "buy-now-button")
    private WebElement buyNowButton;

    @FindBy(id = "quantity")
    private WebElement quantityDropdown;

    @FindBy(css = "#feature-bullets li span.a-list-item")
    private List<WebElement> bulletPoints;

    @FindBy(css = "#availability span")
    private WebElement availabilityStatus;

    @FindBy(id = "imgTagWrapperId")
    private WebElement productImage;

    @FindBy(css = "#variation_color_name .selection")
    private WebElement colorSelection;

    @FindBy(css = "#variation_size_name .selection")
    private WebElement sizeSelection;

    @FindBy(id = "ASIN")
    private WebElement asinField;

    // ─── Confirmation after adding to cart ───────────────────────────────────
    @FindBy(id = "NATC_SMART_WAGON_CONF_MSG_SUCCESS")
    private WebElement addToCartConfirmation;

    @FindBy(id = "nav-cart-count")
    private WebElement cartCount;

    // ─── Search Actions ───────────────────────────────────────────────────────
    public ProductPage searchFor(String keyword) {
        ExtentReportManager.logStep("Searching for: " + keyword);
        type(searchBox, keyword);
        click(searchButton);
        return this;
    }

    public int getSearchResultCount() {
        return searchResults.size();
    }

    public List<String> getAllProductTitles() {
        return productTitles.stream().map(this::getText).collect(Collectors.toList());
    }

    public List<String> getAllProductPrices() {
        return productPrices.stream().map(this::getText).collect(Collectors.toList());
    }

    public void clickFirstProduct() {
        ExtentReportManager.logStep("Clicking first product in results");
        click(productLinks.get(0));
    }

    public void clickProductByIndex(int index) {
        ExtentReportManager.logStep("Clicking product at index: " + index);
        click(productLinks.get(index));
    }

    public void clickNextPage() {
        click(nextPageButton);
    }

    // ─── PDP Actions ──────────────────────────────────────────────────────────
    public String getProductTitle() {
        return getText(productTitle);
    }

    public String getProductPrice() {
        return getText(productPrice);
    }

    public String getStarRating() {
        return getAttribute(starRating, "innerHTML")
                .replaceAll("out of.*", "").trim();
    }

    public String getReviewCount() {
        return getText(reviewCount);
    }

    public String getAvailabilityStatus() {
        return getText(availabilityStatus);
    }

    public boolean isInStock() {
        return getAvailabilityStatus().toLowerCase().contains("in stock");
    }

    public List<String> getBulletPoints() {
        return bulletPoints.stream().map(this::getText).collect(Collectors.toList());
    }

    public String getASIN() {
        return getAttribute(asinField, "value");
    }

    public void selectQuantity(String qty) {
        ExtentReportManager.logStep("Selecting quantity: " + qty);
        selectByValue(quantityDropdown, qty);
    }

    public void selectColor(String color) {
        ExtentReportManager.logStep("Selecting color: " + color);
        By colorBtn = By.xpath("//li[contains(@id,'color_name')]//img[@alt='" + color + "']");
        clickByLocator(colorBtn);
    }

    public void selectSize(String size) {
        ExtentReportManager.logStep("Selecting size: " + size);
        By sizeBtn = By.xpath("//li[contains(@id,'size_name')]//span[text()='" + size + "']");
        clickByLocator(sizeBtn);
    }

    public void addToCart() {
        ExtentReportManager.logStep("Clicking Add to Cart");
        scrollToElement(addToCartButton);
        click(addToCartButton);
    }

    public void clickBuyNow() {
        ExtentReportManager.logStep("Clicking Buy Now");
        scrollToElement(buyNowButton);
        click(buyNowButton);
    }

    public boolean isAddToCartConfirmed() {
        return isDisplayed(addToCartConfirmation);
    }

    public int getCartCount() {
        try {
            return Integer.parseInt(getText(cartCount));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ─── Contract ─────────────────────────────────────────────────────────────
    @Override
    public boolean isLoaded() {
        return isDisplayed(searchBox);
    }
}
