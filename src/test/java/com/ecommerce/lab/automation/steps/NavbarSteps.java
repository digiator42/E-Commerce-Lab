package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.components.Navbar;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.Then;

public class NavbarSteps {

    private WebDriver getDriver() { return DriverManager.getSmartDriver(); }

    @Then("the cart badge should display {string}")
    public void the_cart_badge_should_display(String expectedCount) {
        Navbar navbar = new Navbar(getDriver());
        int actualCount = navbar.getCartCount();
        assertThat(actualCount, is(Integer.parseInt(expectedCount)));
    }

    @Then("the user menu should be visible")
    public void the_user_menu_should_be_visible() {
        Navbar navbar = new Navbar(getDriver());
        assertThat(navbar.isAuthenticated(), is(true));
    }
}