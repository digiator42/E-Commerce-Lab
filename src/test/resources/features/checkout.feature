
Feature: Parallel Checkout Verification

  @Parallel
  Scenario Outline: Concurrent checkout with different accounts
    Given the user "<email>" with "<password>" is logged in
    And one item is added to the cart
    When the user proceeds to checkout
    Then the order should be successful for "<email>"

    Examples:
      | email               | password     |
      | hassan@example.com  | password123  |
      | farag@example.com   | password123  |