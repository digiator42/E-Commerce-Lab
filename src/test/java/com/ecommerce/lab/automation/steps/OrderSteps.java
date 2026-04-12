package com.ecommerce.lab.automation.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.Then;

public class OrderSteps {

    @Then("the order should be successful for {string}")
    public void the_order_should_be_successful_for(String string) {
        WebDriver driver = DriverManager.getDriver(true);

        SeleniumUtils.waitAndClick(driver, By.id("checkout-submit-btn"), 10);

    }

}
