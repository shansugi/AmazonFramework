# /src/test/resources/features/cart/Cart.feature
# Owned by: @cart-team

@cart @smoke
Feature: Amazon Shopping Cart

  Background:
    Given I am logged in as a registered user
    And I have added "wireless mouse" to my cart

  @p1 @regression
  Scenario: Verify product is added to cart
    When I navigate to the cart
    Then the cart should contain 1 item
    And the cart subtotal should be displayed

  @p1
  Scenario: Update item quantity in cart
    When I navigate to the cart
    And I update the quantity of item 1 to "2"
    Then the cart subtotal should update accordingly

  @p2
  Scenario: Remove item from cart
    When I navigate to the cart
    And I delete item at index 0
    Then the cart should be empty

  @p2
  Scenario: Save item for later
    When I navigate to the cart
    And I save item 0 for later
    Then the cart items count should decrease
    And saved for later count should increase

  @p2
  Scenario: Apply valid coupon code
    When I navigate to the cart
    And I apply coupon code "SAVE10"
    Then the coupon should be applied successfully

  @p3
  Scenario: Apply invalid coupon code shows error
    When I navigate to the cart
    And I apply coupon code "INVALIDCODE"
    Then I should see a coupon error message

  @p1
  Scenario: Proceed to checkout from cart
    When I navigate to the cart
    And I click Proceed to Checkout
    Then I should be on the checkout page
