# /src/test/resources/features/product/ProductSearch.feature
# Owned by: @product-team

@product @smoke
Feature: Amazon Product Search and Selection

  Background:
    Given I am on the Amazon homepage

  @p1 @regression
  Scenario: Search for a product and view results
    When I search for "wireless headphones"
    Then I should see at least 10 search results
    And product titles should contain "headphone" or "earphone"

  @p1
  Scenario Outline: Search and add product to cart
    When I search for "<product>"
    And I click on the first product
    Then the product detail page should be displayed
    And I should see the product title
    And I should see the product price
    When I click Add to Cart
    Then the cart count should increase by 1

    Examples:
      | product              |
      | USB-C cable          |
      | laptop stand         |
      | bluetooth speaker    |

  @p2
  Scenario: Verify product details on PDP
    When I search for "iPhone 15 case"
    And I click on the first product
    Then I should see product title
    And I should see product price
    And I should see star rating
    And I should see review count
    And I should see availability status

  @p2 @data-driven
  Scenario Outline: Search multiple products from Excel data
    When I search for "<keyword>"
    Then I should see at least "<minResults>" search results
    And the first result price should be less than "<maxPrice>"

    Examples: loaded from Excel
      | keyword        | minResults | maxPrice |
      | laptop         | 20         | 150000   |
      | smartphone     | 15         | 80000    |
      | tablet         | 10         | 60000    |

  @p3
  Scenario: Filter products by category
    When I search for "shoes"
    And I filter by category "Men's Shoes"
    Then all results should belong to "Men's Shoes" category

  @p3
  Scenario: Navigate to next page of results
    When I search for "books"
    Then I should see search results
    When I click next page
    Then I should be on page 2 of results
