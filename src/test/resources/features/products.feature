@products
Feature: Product Browsing and Filtering
  As a customer
  I want to browse and filter products
  So that I can find items I want to purchase

  Background:
    Given the user is on the products page

  @search
  Scenario: User searches for a product
    When the user searches for "Basketball"
    Then at least one product should be displayed
    And all displayed products should contain "Basketball" in their name

  @category-filter
  Scenario: User filters by category
    When the user selects category "Electronics"
    Then all displayed products should have category "Electronics"

  @price-filter
  Scenario: User filters by price range
    When the user navigates to products with min price "50" and max price "100"
    Then all displayed products should have price between 50 and 100

  @sorting
  Scenario Outline: User sorts products
    When the user sorts by "<sortOption>"
    Then products should be displayed in correct order

    Examples:
      | sortOption           |
      | Price (Low to High)  |
      | Price (High to Low)  |
      | Name (A-Z)           |

  @pagination
  Scenario: User navigates through pages
    When the user goes to the next page
    Then the page indicator should show "Page 2"
    When the user goes to the previous page
    Then the page indicator should show "Page 1"

  @stock-filter
  Scenario: User filters in-stock items only
    When the user toggles "In Stock Only"
    Then all displayed products should be in stock