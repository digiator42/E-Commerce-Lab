package com.ecommerce.lab.automation.steps;

import io.cucumber.java.en.Given;

import com.ecommerce.lab.automation.pages.LoginPage;
import com.ecommerce.lab.automation.utils.DriverManager;

public class LoginSteps {

    LoginPage loginPage = new LoginPage(DriverManager.getParallelDriverWait(true));

    @Given("the user {string} with {string} is logged in")
    public void site_is_open(String email, String password) {
        loginPage.login(email, password);
    }
}