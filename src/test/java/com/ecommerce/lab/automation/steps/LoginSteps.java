package com.ecommerce.lab.automation.steps;

import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class LoginSteps {

    @Given("the user {string} with {string} is logged in")
    public void site_is_open(String email, String password) {

        WebDriver driver = DriverManager.getDriver(true);
        // Open Site
        SeleniumUtils.navigateTo(driver, "/login");
        // Login
        SeleniumUtils.waitForElement(driver, By.id("login-email"), 10).sendKeys(email);
        SeleniumUtils.waitForElement(driver, By.id("login-password"), 10).sendKeys(password);
        SeleniumUtils.waitAndClick(driver, By.id("login-btn"), 10);

    }
}