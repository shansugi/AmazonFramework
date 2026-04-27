package com.amazon.checkout.pages;

import com.amazon.framework.base.BasePage;
import com.amazon.framework.listeners.ExtentReportManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * CheckoutPage — multi-step checkout: address → payment → review → confirm.
 * Owned by: @checkout-team
 * CODEOWNERS: /src/test/java/com/amazon/checkout/ @checkout-team
 */
public class CheckoutPage extends BasePage {

    // ─── Step 1: Delivery Address ─────────────────────────────────────────────
    @FindBy(id = "address-ui-widgets-enterAddressFullName")
    private WebElement fullNameField;

    @FindBy(id = "address-ui-widgets-enterAddressPhoneNumber")
    private WebElement phoneField;

    @FindBy(id = "address-ui-widgets-enterAddressLine1")
    private WebElement addressLine1;

    @FindBy(id = "address-ui-widgets-enterAddressLine2")
    private WebElement addressLine2;

    @FindBy(id = "address-ui-widgets-enterAddressCity")
    private WebElement cityField;

    @FindBy(id = "address-ui-widgets-enterAddressStateOrRegion")
    private WebElement stateDropdown;

    @FindBy(id = "address-ui-widgets-enterAddressPostalCode")
    private WebElement pincodeField;

    @FindBy(css = "[data-testid='address-book-button-deliver-here']")
    private WebElement deliverHereButton;

    @FindBy(css = ".address-book-entry .a-radio-label")
    private WebElement useExistingAddressRadio;

    @FindBy(id = "add-new-address-popover-link")
    private WebElement addNewAddressLink;

    // ─── Step 2: Payment ──────────────────────────────────────────────────────
    @FindBy(id = "ppw-instrumentName-CREDIT_CARD")
    private WebElement creditCardOption;

    @FindBy(id = "addCreditCardNumber")
    private WebElement cardNumberField;

    @FindBy(id = "pp-AxXA7w-11")  // name on card
    private WebElement cardNameField;

    @FindBy(id = "addExpirationMonth")
    private WebElement expiryMonthDropdown;

    @FindBy(id = "addExpirationYear")
    private WebElement expiryYearDropdown;

    @FindBy(id = "addCreditCardVerificationNumber")
    private WebElement cvvField;

    @FindBy(css = "#payment-continue-btn .a-button-input")
    private WebElement paymentContinueButton;

    @FindBy(css = "[data-feature-id='payment-cashondelivery']")
    private WebElement codOption;

    // ─── Step 3: Review Order ─────────────────────────────────────────────────
    @FindBy(css = ".order-summary-shipping-cost .a-price")
    private WebElement shippingCost;

    @FindBy(css = "#subtotals-marketplace-table .grand-total-price")
    private WebElement orderTotal;

    @FindBy(id = "estimated-delivery-date")
    private WebElement estimatedDeliveryDate;

    @FindBy(id = "placeYourOrder")
    private WebElement placeOrderButton;

    @FindBy(css = ".checkout-header-title")
    private WebElement checkoutTitle;

    // ─── Step 4: Confirmation ─────────────────────────────────────────────────
    @FindBy(css = ".a-alert-success")
    private WebElement orderSuccessAlert;

    @FindBy(css = "#widget-purchaseConfirmationStatus .a-size-medium-plus")
    private WebElement orderConfirmationMessage;

    @FindBy(css = "[data-component-type='s-order-id']")
    private WebElement orderId;

    // ─── Address Actions ──────────────────────────────────────────────────────
    public CheckoutPage enterFullName(String name) {
        ExtentReportManager.logStep("Entering full name: " + name);
        type(fullNameField, name);
        return this;
    }

    public CheckoutPage enterPhone(String phone) {
        type(phoneField, phone);
        return this;
    }

    public CheckoutPage enterAddressLine1(String address) {
        type(addressLine1, address);
        return this;
    }

    public CheckoutPage enterCity(String city) {
        type(cityField, city);
        return this;
    }

    public CheckoutPage selectState(String state) {
        selectByVisibleText(stateDropdown, state);
        return this;
    }

    public CheckoutPage enterPincode(String pincode) {
        type(pincodeField, pincode);
        return this;
    }

    public void clickDeliverHere() {
        ExtentReportManager.logStep("Clicking Deliver Here");
        click(deliverHereButton);
    }

    public void useExistingAddress() {
        ExtentReportManager.logStep("Using existing saved address");
        click(useExistingAddressRadio);
        click(deliverHereButton);
    }

    /**
     * Complete address entry in one call.
     */
    public void fillShippingAddress(String name, String phone, String address,
                                     String city, String state, String pincode) {
        ExtentReportManager.logStep("Filling shipping address for: " + name);
        enterFullName(name);
        enterPhone(phone);
        enterAddressLine1(address);
        enterCity(city);
        selectState(state);
        enterPincode(pincode);
        clickDeliverHere();
    }

    // ─── Payment Actions ──────────────────────────────────────────────────────
    public void selectCreditCard() {
        ExtentReportManager.logStep("Selecting credit card payment");
        click(creditCardOption);
    }

    public void selectCashOnDelivery() {
        ExtentReportManager.logStep("Selecting Cash on Delivery");
        click(codOption);
        click(paymentContinueButton);
    }

    public void enterCardDetails(String cardNumber, String name, String month,
                                  String year, String cvv) {
        ExtentReportManager.logStep("Entering card details");
        type(cardNumberField, cardNumber);
        type(cardNameField, name);
        selectByVisibleText(expiryMonthDropdown, month);
        selectByVisibleText(expiryYearDropdown, year);
        type(cvvField, cvv);
        click(paymentContinueButton);
    }

    // ─── Review & Place Order ─────────────────────────────────────────────────
    public String getOrderTotal() {
        return getText(orderTotal);
    }

    public String getShippingCost() {
        return getText(shippingCost);
    }

    public String getEstimatedDelivery() {
        return getText(estimatedDeliveryDate);
    }

    public void placeOrder() {
        ExtentReportManager.logStep("Placing final order");
        scrollToElement(placeOrderButton);
        click(placeOrderButton);
    }

    // ─── Confirmation ─────────────────────────────────────────────────────────
    public boolean isOrderPlaced() {
        return isDisplayed(orderSuccessAlert) || isDisplayed(orderConfirmationMessage);
    }

    public String getOrderId() {
        return getText(orderId).replaceAll("[^0-9\\-]", "").trim();
    }

    public String getConfirmationMessage() {
        return getText(orderConfirmationMessage);
    }

    // ─── Contract ─────────────────────────────────────────────────────────────
    @Override
    public boolean isLoaded() {
        return getCurrentUrl().contains("checkout") && isDisplayed(checkoutTitle);
    }
}
