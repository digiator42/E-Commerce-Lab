
Feature: Parallel Checkout Verification

  @Parallel
  Scenario Outline: Concurrent checkout with different accounts
    Given the user "<email>" with "<password>" is logged in
    And user adds product "<productId>" to the cart
    When the user proceeds to checkout
    Then the order should be successful for "<email>"

    Examples:
      | email               | password     | productId |
      | hassan@example.com  | password123  | 35        |     
      | farag@example.com   | password123  | 35        |