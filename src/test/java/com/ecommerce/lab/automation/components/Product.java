package com.ecommerce.lab.automation.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import lombok.Data;

@Data
public class Product {
    private final WebElement root;

    // Locators scoped to the product container
    private final By title = By.cssSelector("h3");
    private final By price = By.xpath(".//span[contains(text(), '$')]");
    // private final By category = By.xpath(".//span[last()]"); // The 'Sports'
    private final By category = By
        .xpath(".//span[contains(@class, 'bg-gray-100') and contains(@class, 'rounded-full')]");
    private final By description = By.cssSelector("p.text-gray-500");
    private final By addToCartBtn = By
        .xpath(".//button[contains(@onclick, 'cartManager.addItem')]");
    private final By wishlistBtn = By
        .xpath(".//button[contains(@onclick, 'wishlistManager.toggleItem')]");
    private final By reviewCount = By.cssSelector(".text-xs.text-gray-400");

    public Product(WebElement root) { this.root = root; }

    public String getName() { return root.findElement(title).getText(); }

    public String getCategoryValue() { return root.findElement(category).getText(); }

    public String getDescriptionValue() { return root.findElement(description).getText(); }

    public void addToCart() { root.findElement(addToCartBtn).click(); }

    public void toggleWishlist() { root.findElement(wishlistBtn).click(); }

    public Double getPriceValue() {
        String priceText = root.findElement(price).getText();
        return Double.parseDouble(priceText.replaceAll("[^0-9.]", ""));
    }

    public Integer getProductId() {
        // Extracting id from "window.cartManager.addItem(72)"
        String attr = root.findElement(addToCartBtn).getAttribute("onclick");
        return Integer.parseInt(attr.replaceAll("\\D", ""));
    }

    public int getReviewCountValue() {
        String text = root.findElement(reviewCount).getText();
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }

}