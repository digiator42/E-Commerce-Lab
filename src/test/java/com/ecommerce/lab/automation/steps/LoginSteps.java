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
        driver.findElement(By.id("login-email")).sendKeys(email);
        driver.findElement(By.id("login-password")).sendKeys(password);
        driver.findElement(By.id("login-btn")).click();

    }
}