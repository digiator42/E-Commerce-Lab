package com.ecommerce.lab.automation.steps;

import io.cucumber.java.en.Given;

import com.ecommerce.lab.automation.pages.LoginPage;
import com.ecommerce.lab.automation.utils.DriverManager;

public class LoginSteps {

    LoginPage loginPage = new LoginPage(DriverManager.getDriver(true));

    @Given("the user {string} with {string} is logged in")
    public void site_is_open(String email, String password) {

        // // Open Site
        // SeleniumUtils.navigateTo(driver, "/login");
        // // Login
        // SeleniumUtils.waitForElement(driver, By.id("login-email"), 10).sendKeys(email);
        // SeleniumUtils.waitForElement(driver, By.id("login-password"), 10).sendKeys(password);
        // SeleniumUtils.waitAndClick(driver, By.id("login-btn"), 10);

        loginPage.login(email, password);
    }
}