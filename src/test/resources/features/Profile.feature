@profile @user-management
Feature: User Profile Management
  As a registered user
  I want to manage my profile settings
  So that I can keep my account information up to date

  Background:
    Given the user "hassan@example.com" with "password123" is logged in
    And the user is on the profile page

  @view-profile
  Scenario: User views their profile information
    Then the profile page should be displayed
    And the page title should contains "MASTERSHOP"
    And the profile display name should be "Ahmed Hassan"
    And the profile email should be "hassan@example.com"
    And all profile tabs should be present
    And the last login time should be displayed

  @update-display-name
  Scenario: User updates their display name
    When the user clicks on Personal Info tab
    And the user enters display name "Ahmed Hassan Updated"
    And the user submits the personal info form
    Then the profile display name should be "Ahmed Hassan Updated"
    And the display name field should contain "Ahmed Hassan Updated"

  @email-readonly
  Scenario: Email field is not editable
    When the user clicks on Personal Info tab
    Then the email field should be readonly

  @update-address
  Scenario: User updates shipping address
    When the user clicks on Shipping Address tab
    And the user fills address with "123 Main St", "New York", "NY", "10001", "USA"
    And the user submits the address form
    Then the street field should contain "123 Main St"
    And the city field should contain "New York"
    And the state field should contain "NY"
    And the zip field should contain "10001"
    And the country field should contain "USA"

  @password-fields-masked
  Scenario: Password fields are masked for security
    When the user clicks on Security tab
    Then the password fields should be of type password

  @view-store-balance
  Scenario: User views their store balance
    When the user clicks on Store Balance tab
    Then the store balance should be displayed
    And the redeem form should be visible

  @redeem-gift-card
  Scenario: User redeems a gift card
    When the user clicks on Store Balance tab
    And the user enters gift card code "RAMADAN20"
    And the user clicks the redeem button
    Then the redeem history should show activity

  @redeem-invalid-code
  Scenario: User attempts to redeem invalid gift card code
    When the user clicks on Store Balance tab
    And the user enters gift card code "INVALID"
    And the user clicks the redeem button
    Then an error message "Gift card not found" should be displayed

  @tab-navigation
  Scenario Outline: User navigates between profile tabs
    When the user clicks on the "<tab>" tab
    Then the "<tab>" tab should be visible
    And the active tab should be "<tab>"

    Examples:
      | tab              |
      | info             |
      | address          |
      | security         |
      | 2fa              |
      | redeem           |

  @change-password @skip
  Scenario: User changes their password
    When the user clicks on Security tab
    And the user enters current password "password123"
    And the user enters new password "NewPass@456"
    And the user confirms password "NewPass@456"
    And the user submits the password change
    Then a success message should be displayed