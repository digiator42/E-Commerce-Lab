package com.ecommerce.lab.automation.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.Given;

public class CartSteps {
    @Given("one item is added to the cart")
    public void one_item_is_added_to_the_cart() {
        WebDriver driver = DriverManager.getDriver(true);

        SeleniumUtils.hardWaitAndClick(driver, By.xpath("//button[contains(@onclick, 'addItem(36)')]"), 10);
    }
}
