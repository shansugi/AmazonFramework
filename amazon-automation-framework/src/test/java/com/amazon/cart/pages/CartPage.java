package com.amazon.cart.pages;

import com.amazon.framework.base.BasePage;
import com.amazon.framework.listeners.ExtentReportManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CartPage — Shopping cart management.
 * Owned by: @cart-team
 * CODEOWNERS: /src/test/java/com/amazon/cart/ @cart-team
 */
public class CartPage extends BasePage {

    // ─── Cart Items ───────────────────────────────────────────────────────────
    @FindBy(css = ".sc-list-item")
    private List<WebElement> cartItems;

    @FindBy(css = ".sc-product-title span")
    private List<WebElement> cartItemTitles;

    @FindBy(css = ".sc-product-price .a-offscreen")
    private List<WebElement> cartItemPrices;

    @FindBy(css = ".sc-action-quantity select")
    private List<WebElement> quantitySelectors;

    @FindBy(css = ".sc-action-delete input")
    private List<WebElement> deleteButtons;

    @FindBy(css = ".sc-action-save-for-later input")
    private List<WebElement> saveForLaterButtons;

    // ─── Cart Summary ──────────────────────────────────────────────────────────
    @FindBy(id = "sc-subtotal-amount-activecart")
    private WebElement subtotalAmount;

    @FindBy(id = "sc-subtotal-label-activecart")
    private WebElement subtotalItemCount;

    @FindBy(css = "#activeCartViewForm .a-button-input")
    private WebElement proceedToCheckoutButton;

    // ─── Empty cart ───────────────────────────────────────────────────────────
    @FindBy(css = ".sc-your-amazon-cart-is-empty")
    private WebElement emptyCartMessage;

    // ─── Saved for later ──────────────────────────────────────────────────────
    @FindBy(css = ".sc-list-item-saved")
    private List<WebElement> savedItems;

    // ─── Coupon ───────────────────────────────────────────────────────────────
    @FindBy(id = "spc-coupon-code-input")
    private WebElement couponCodeInput;

    @FindBy(id = "spc-coupon-code-button")
    private WebElement applyCouponButton;

    @FindBy(css = ".coupon-success-msg")
    private WebElement couponSuccessMessage;

    @FindBy(css = ".coupon-error-msg")
    private WebElement couponErrorMessage;

    // ─── Actions ──────────────────────────────────────────────────────────────
    public int getCartItemCount() {
        return cartItems.size();
    }

    public List<String> getCartItemTitles() {
        return cartItemTitles.stream().map(this::getText).collect(Collectors.toList());
    }

    public List<String> getCartItemPrices() {
        return cartItemPrices.stream().map(this::getText).collect(Collectors.toList());
    }

    public String getSubtotal() {
        return getText(subtotalAmount);
    }

    public boolean isCartEmpty() {
        return isDisplayed(emptyCartMessage);
    }

    public void updateQuantity(int itemIndex, String quantity) {
        ExtentReportManager.logStep("Updating quantity of item " + itemIndex + " to " + quantity);
        selectByValue(quantitySelectors.get(itemIndex), quantity);
    }

    public void deleteItem(int itemIndex) {
        ExtentReportManager.logStep("Deleting cart item at index: " + itemIndex);
        click(deleteButtons.get(itemIndex));
    }

    public void deleteAllItems() {
        ExtentReportManager.logStep("Deleting all cart items");
        while (!deleteButtons.isEmpty()) {
            click(deleteButtons.get(0));
            waitForInvisible(cartItems.get(0));
        }
    }

    public void saveItemForLater(int itemIndex) {
        ExtentReportManager.logStep("Saving item " + itemIndex + " for later");
        click(saveForLaterButtons.get(itemIndex));
    }

    public int getSavedItemCount() {
        return savedItems.size();
    }

    public void applyCoupon(String code) {
        ExtentReportManager.logStep("Applying coupon: " + code);
        type(couponCodeInput, code);
        click(applyCouponButton);
    }

    public boolean isCouponApplied() {
        return isDisplayed(couponSuccessMessage);
    }

    public String getCouponError() {
        return getText(couponErrorMessage);
    }

    public void proceedToCheckout() {
        ExtentReportManager.logStep("Proceeding to checkout");
        scrollToElement(proceedToCheckoutButton);
        click(proceedToCheckoutButton);
    }

    // ─── Contract ─────────────────────────────────────────────────────────────
    @Override
    public boolean isLoaded() {
        return getCurrentUrl().contains("cart") && isDisplayed(proceedToCheckoutButton);
    }
}
