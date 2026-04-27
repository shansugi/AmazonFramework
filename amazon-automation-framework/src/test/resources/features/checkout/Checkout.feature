# /src/test/resources/features/checkout/Checkout.feature
# Owned by: @checkout-team

@checkout @smoke
Feature: Amazon Checkout Flow

  Background:
    Given I am logged in as a registered user
    And I have a product in my cart

  @p1 @regression
  Scenario: Complete checkout with existing address and COD
    When I proceed to checkout
    And I use my existing delivery address
    And I select Cash on Delivery as payment
    Then I should see the order review page
    And I should see the order total
    When I place the order
    Then the order should be placed successfully
    And I should see an order confirmation

  @p1 @data-driven
  Scenario Outline: Checkout with new delivery address
    When I proceed to checkout
    And I add a new delivery address:
      | name    | <name>    |
      | phone   | <phone>   |
      | address | <address> |
      | city    | <city>    |
      | state   | <state>   |
      | pincode | <pincode> |
    And I select Cash on Delivery as payment
    And I place the order
    Then the order should be placed successfully

    Examples:
      | name        | phone      | address         | city      | state       | pincode |
      | John Smith  | 9876543210 | 123 MG Road     | Bangalore | Karnataka   | 560001  |
      | Jane Doe    | 8765432109 | 456 Park Street | Mumbai    | Maharashtra | 400001  |

  @p2
  Scenario: Verify order total on review page
    When I proceed to checkout
    And I use my existing delivery address
    And I select Cash on Delivery as payment
    Then the order total on review page should match the cart total

  @p2
  Scenario: Verify estimated delivery date is shown
    When I proceed to checkout
    And I use my existing delivery address
    And I select Cash on Delivery as payment
    Then I should see an estimated delivery date

  @p3
  Scenario: Back navigation during checkout preserves cart
    When I proceed to checkout
    And I navigate back to cart
    Then my cart items should still be present
