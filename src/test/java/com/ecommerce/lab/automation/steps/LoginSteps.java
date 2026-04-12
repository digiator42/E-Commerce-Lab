package com.ecommerce.lab.automation.steps;

import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;

import com.ecommerce.lab.automation.utils.DriverManager;

public class LoginSteps {

    @Given("the user {string} with {string} is logged in")
    public void site_is_open(String email, String password) {

        WebDriver driver = DriverManager.getDriverWait(true);
        // Open Site
        driver.get("https://e-commerce-lab.onrender.com/login");
        // Login
        driver.findElement(By.id("login-email")).sendKeys(email);
        driver.findElement(By.id("login-password")).sendKeys(password);
        driver.findElement(By.id("login-btn")).click();

    }    
}