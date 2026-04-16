@Checkout
Feature: Checkout
  As a logged-in user
  I want to complete the checkout process
  So that I can place an order with correct items, address, and payment details

  @Parallel @Checkout @Smoke @E2E
  Scenario Outline: Concurrent checkout with different accounts
    Given the user "<email>" with "<password>" is logged in
    And user adds product "<productId>" to the cart
    When the user proceeds to checkout
    Then the order should be successful for "<email>"

    Examples:
      | email               | password     | productId |
      | hassan@example.com  | password123  | 71        |     
      | farag@example.com   | password123  | 71        |
