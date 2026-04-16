Feature: Cart Management
  
  @cart @Smoke
  Scenario: Add a product to the cart successfully
    Given the user is on the home page
    When the user adds "Water Bottle" to the shopping cart
    Then the cart badge should display one item