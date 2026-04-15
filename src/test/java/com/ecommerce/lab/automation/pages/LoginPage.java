package com.ecommerce.lab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class LoginPage {
    private WebDriver driver;

    // Locators
    private final By emailField = By.id("login-email");
    private final By passwordField = By.id("login-password");
    private final By loginButton = By.cssSelector("button[type='submit']");

    public LoginPage(WebDriver driver) { this.driver = driver; }

    // Actions
    public void login(String email, String password) {

        SeleniumUtils.get(driver, "/login");

        SeleniumUtils.waitAndSendKeys(driver, emailField, email);
        SeleniumUtils.waitAndSendKeys(driver, passwordField, password);
        SeleniumUtils.waitAndClick(driver, loginButton, 10);
    }
}