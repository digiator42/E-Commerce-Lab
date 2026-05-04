@home @ui
Feature: Home Page Display
  As a customer
  I want to view the home page
  So that I can discover products and promotions

  Background:
    Given the user is on the home page

  @navigate-from-home
  Scenario Outline: User navigates from home page to other sections
    When the user clicks on "<element>"
    Then the user should be on the "<destination>" page

    Examples:
      | element               | destination |
      | Shop Ramadan Deals    | products    |
      | Gift Cards in hero    | gift-cards  |
      | Electronics View All  | products    |
      | Fashion View All      | products    |
      | Home View All         | products    |

  @add-to-cart-from-home
  Scenario: User adds product to cart directly from home page
    When the user adds "Smartphone" to cart from home page
    Then the cart badge should display "1"