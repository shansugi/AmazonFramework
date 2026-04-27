# /src/test/resources/features/tracking/TrackOrder.feature
# Owned by: @orders-team

@tracking @smoke
Feature: Amazon Track My Order

  Background:
    Given I am logged in as a registered user

  @p1 @regression
  Scenario: View order history
    When I navigate to My Orders
    Then I should see my order history

  @p1
  Scenario: Track most recent order
    When I navigate to My Orders
    And I click on the most recent order
    And I click Track Package
    Then I should see the tracking status
    And I should see tracking history events

  @p2
  Scenario: Verify delivery promise on tracked order
    When I navigate to My Orders
    And I click on the most recent order
    And I click Track Package
    Then I should see the delivery promise

  @p2
  Scenario: Verify order placed after checkout appears in history
    When I navigate to My Orders
    Then I should see my order history
    And the order with ID "" should be in my orders

  @p3
  Scenario: Cancel a cancellable order
    When I navigate to My Orders
    And I click on the most recent order
    Then I should be able to cancel the order
