package com.ecommerce.lab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class ProductPage {
    private WebDriver driver;

    private By productButton(String id) {
        return By.xpath("//button[contains(@onclick, 'addItem(" + id + ")')]");
    }

    public ProductPage(WebDriver driver) { this.driver = driver; }

    public void addProductToCart(String productId) {
        SeleniumUtils.waitAndClick(driver, productButton(productId), SeleniumUtils.DEFAULT_TIMEOUT);
    }
}