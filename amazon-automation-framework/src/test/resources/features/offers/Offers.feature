# /src/test/resources/features/offers/Offers.feature
# Owned by: @offers-team

@offers @smoke
Feature: Amazon Today's Deals and Offers

  Background:
    Given I am on the Amazon homepage

  @p1 @regression
  Scenario: View today's deals page
    When I navigate to Today's Deals
    Then I should see deal cards on the page
    And each deal card should have a title and price

  @p1
  Scenario: Lightning deals are visible with countdown timers
    When I navigate to Today's Deals
    Then I should see lightning deals with timers

  @p2
  Scenario: Filter deals by Electronics category
    When I navigate to Today's Deals
    And I filter deals by "Electronics"
    Then I should see deals for "Electronics" category

  @p2
  Scenario: Clip a coupon from deals page
    When I navigate to Today's Deals
    And I clip a coupon at position 1
    Then the coupon should be clipped successfully

  @p3
  Scenario: Click a deal card navigates to product page
    When I navigate to Today's Deals
    And I click on deal at position 1
    Then I should be on a product page

  @p2
  Scenario Outline: Filter deals by multiple categories
    When I navigate to Today's Deals
    And I filter deals by "<category>"
    Then I should see deals for "<category>" category

    Examples:
      | category    |
      | Electronics |
      | Fashion     |
      | Home        |
      | Books       |
