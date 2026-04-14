package com.ecommerce.lab.automation.pages;

import java.util.List;
import java.util.function.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ecommerce.lab.automation.components.Product;
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

    public List<Product> getProducts() {
        List<WebElement> productElements = SeleniumUtils.waitForAllElements(
            driver,
            By.cssSelector("#product-list-container .group"),
            SeleniumUtils.DEFAULT_TIMEOUT
        );

        return productElements.stream()
            .map(Product::new)
            .toList();
    }

    public Product getProduct(Predicate<Product> condition) {
        List<Product> products = getProducts();

        return products
            .stream()
            .filter(condition) // Filter by product name or price
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}