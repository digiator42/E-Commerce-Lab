package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ecommerce.lab.automation.components.Navbar;
import com.ecommerce.lab.automation.pages.LoginPage;
import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class LoginSteps {

    private LoginPage loginPage;
    private WebDriver driver;

    // Locators
    private final By toastContainer = By.cssSelector("div.bg-red-600");
    private final By toastMessage = By.cssSelector("span.flex-1");
    private final By toastCloseButton = By.cssSelector("button.ml-3");

    private final By loginForm = By.cssSelector("button[type='submit']");

    private WebDriver getDriver() {
        if (driver == null) {
            driver = DriverManager.getSmartDriver();
        }
        return driver;
    }

    private LoginPage getLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(getDriver());
        }
        return loginPage;
    }

    @Given("the user {string} with {string} is logged in")
    public void site_is_open(String email, String password) {
        getLoginPage().login(email, password);
        SeleniumUtils.pause(2000);

        // Verify login was successful by checking navbar
        Navbar navbar = new Navbar(getDriver());
        assertThat(
            "Login failed - user is not authenticated",
            navbar.isAuthenticated(), is(true)
        );

    }

    @And("the username should display {string}")
    public void the_username_should_display(String expectedDisplayName) {
        Navbar navbar = new Navbar(getDriver());

        // First verify user is authenticated
        boolean isAuthenticated = navbar.isAuthenticated();
        assertThat("User is not authenticated", isAuthenticated, is(true));

        // Then get and verify the displayed username
        String actualDisplayName = navbar.getUserNameDisplay();
        assertThat(
            "Username display does not match expected value",
            actualDisplayName, is(expectedDisplayName)
        );

    }

    @When("the user attempts to login with {string} and {string}")
    public void the_user_attempts_to_login_with_and(String email, String password) {
        SeleniumUtils.navigateTo(getDriver(), "/login");
        SeleniumUtils.pause(1000);
        getLoginPage().login(email, password);
    }

    @Then("an error message {string} should be displayed")
    public void an_error_message_should_be_displayed(String expectedErrorMessage) {
        WebElement errorElement = SeleniumUtils.waitForElement(
            getDriver(),
            toastMessage,
            SeleniumUtils.DEFAULT_TIMEOUT
        );

        String actualErrorMessage = errorElement.getText().trim();
        assertThat(
            "Error message mismatch",
            actualErrorMessage, containsString(expectedErrorMessage)
        );
    }

    @And("the user should remain on the login page")
    public void the_user_should_remain_on_the_login_page() {
        String currentUrl = getDriver().getCurrentUrl();
        assertThat("URL does not contain /login", currentUrl, containsString("/login"));

        WebElement form = getDriver().findElement(loginForm);
        assertThat("Login form not visible", form.isDisplayed(), is(true));

        // Verify user is NOT authenticated
        Navbar navbar = new Navbar(getDriver());
        assertThat(
            navbar.isOnLoginPage(), is(true)
        );
    }

    @When("the user clicks the logout button")
    public void the_user_clicks_the_logout_button() {
        Navbar navbar = new Navbar(getDriver());
        navbar.clickLogout();
        SeleniumUtils.pause(1000);
    }

    @Then("the auth buttons should be visible")
    public void the_auth_buttons_should_be_visible() {
        Navbar navbar = new Navbar(getDriver());
        assertThat(
            "Auth buttons are not visible after logout",
            navbar.areAuthButtonsVisible(), is(true)
        );
    }

    @And("the user should be redirected to the login page")
    public void the_user_should_be_redirected_to_the_login_page() {
        SeleniumUtils.pause(1000);

        Navbar navbar = new Navbar(getDriver());
        assertThat(
            "Not redirected to login page",
            navbar.isOnLoginPage(), is(true)
        );
    }

}