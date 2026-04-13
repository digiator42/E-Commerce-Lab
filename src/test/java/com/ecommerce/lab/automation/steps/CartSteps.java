package com.ecommerce.lab.automation.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.And;

public class CartSteps {
    @And("user adds product {string} to the cart")
    public void one_item_is_added_to_the_cart(String productId) {
        WebDriver driver = DriverManager.getDriver(true);

        By locator = By.xpath("//button[contains(@onclick, 'addItem(" + productId + ")')]");

        SeleniumUtils.hardWaitAndClick(driver, locator, 10);
    }
}
