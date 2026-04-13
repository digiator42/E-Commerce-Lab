package com.ecommerce.lab.automation.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.When;

public class CheckoutSteps {

    @When("the user proceeds to checkout")
    public void the_user_proceeds_to_checkout() {
        WebDriver driver = DriverManager.getDriver(true);

        SeleniumUtils.hardWaitAndClick(driver, By.id("checkout-btn"), 10);
    }

}
