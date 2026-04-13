package com.ecommerce.lab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class CheckoutPage {
    private WebDriver driver;

    private final By checkoutBtn = By.id("checkout-btn");
    private final By placeOrderBtn = By.id("checkout-submit-btn");
    private final By successOrderId = By.id("success-order-id");

    public CheckoutPage(WebDriver driver) { this.driver = driver; }

    public void proceedToCheckout() {
        // First click on checkout button
        SeleniumUtils.hardWaitAndClick(driver, checkoutBtn, SeleniumUtils.DEFAULT_TIMEOUT);
        // Then click on place order button
        SeleniumUtils.waitAndClick(driver, placeOrderBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    public boolean isOrderSuccessful() {
        return SeleniumUtils.waitForElement(driver, successOrderId, SeleniumUtils.DEFAULT_TIMEOUT)
            .isDisplayed();
    }
}