@authentication @smoke
Feature: User Authentication
  As a customer
  I want to log in to my account
  So that I can access personalized features

  Background:
    Given the user is on the home page

  @valid-login
  Scenario Outline: User logs in with valid credentials
    Given the user "<email>" with "<password>" is logged in
    Then the user menu should be visible
    And the username should display "<displayName>"

    Examples:
      | email              | password     | displayName |
      | farag@example.com  | password123  | Ahmed Farag |

  @invalid-login
  Scenario Outline: User attempts login with invalid credentials
    When the user attempts to login with "<email>" and "<password>"
    Then an error message "<errorMessage>" should be displayed
    And the user should remain on the login page

    Examples:
      | email              | password     | errorMessage   |
      | invalid@test.com   | wrongpass    | User not found |

  @logout
  Scenario: User logs out successfully
    Given the user "hassan@example.com" with "password123" is logged in
    When the user clicks the logout button
    Then the auth buttons should be visible
    And the user should be redirected to the login page