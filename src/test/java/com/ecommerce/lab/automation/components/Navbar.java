package com.ecommerce.lab.automation.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ecommerce.lab.automation.utils.SeleniumUtils;

import java.time.Duration;

public class Navbar {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By mobileContainer = By.id("mobile-menu");
    private final By hamburgerBtn = By.id("mobile-menu-btn");

    private final By cartBtn = By.xpath("//button[contains(@onclick, 'cartManager.toggle')]");
    private final By wishlistBtn = By.xpath("//button[contains(@onclick, 'toggleDrawer.toggle')]");
    private final By cartCountDesktop = By.id("cart-count-desktop");
    private final By wishlistCountDesktop = By.id("wishlist-count-desktop");
    private final By authButtons = By.id("desktop-auth-buttons");
    private final By userMenu = By.id("desktop-user-menu");
    private final By userNameDisplay = By.id("user-name-display");

    // Links
    private final By homeLink = By.linkText("HOME");
    private final By storeLink = By.linkText("STORE");
    private final By loginLink = By.linkText("LOGIN");

    public Navbar(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void goToStore() {
        if (isMobile()) {
            // Open the hamburger menu
            SeleniumUtils.waitAndClick(driver, hamburgerBtn, SeleniumUtils.DEFAULT_TIMEOUT);
            driver.findElement(storeLink).click();
        } else {
            driver.findElement(storeLink).click();
        }
    }

    private boolean isMobile() {
        return driver.findElement(hamburgerBtn).isDisplayed();
    }

    public void clickLogin() { driver.findElement(loginLink).click(); }

    public boolean isAuthenticated() { return driver.findElement(userMenu).isDisplayed(); }

    public int getCartCount() {
        WebElement count = driver.findElement(cartCountDesktop);
        // If the badge is hidden (count is 0)
        String text = count.getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public void openCart() {
        driver.findElement(cartBtn).click();
    }
}