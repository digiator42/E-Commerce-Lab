package com.ecommerce.lab.automation.steps;

import io.cucumber.java.en.Given;

public class LoginSteps {
    @Given("the browser is open")
    public void the_browser_is_open() {
        System.out.println(">>> Selenium is starting...");
    }
}