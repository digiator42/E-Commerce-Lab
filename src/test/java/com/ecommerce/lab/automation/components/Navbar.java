package com.ecommerce.lab.automation.components;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ecommerce.lab.automation.utils.SeleniumUtils;

import java.time.Duration;
import java.util.List;

public class Navbar {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Mobile
    private final By hamburgerBtn = By.id("mobile-menu-btn");
    private final By mobileMenu = By.id("mobile-menu");
    private final By mobileUserMenu = By.id("mobile-user-menu");
    private final By mobileUserName = By.id("mobile-user-name");
    private final By mobileAuthButtons = By.id("mobile-auth-buttons");
    private final By mobileCartCount = By.id("cart-count-mobile");
    private final By mobileWishlistCount = By.id("wishlist-count-mobile");

    // Desktop
    private final By desktopUserMenu = By.id("desktop-user-menu");
    private final By desktopAuthButtons = By.id("desktop-auth-buttons");
    private final By desktopUserNameDisplay = By.id("user-name-display");
    private final By desktopCartCount = By.id("cart-count-desktop");
    private final By desktopWishlistCount = By.id("wishlist-count-desktop");
    private final By userMenuButton = By.id("user-menu-button");
    private final By userDropdown = By.id("user-dropdown");

    // Common
    private final By cartBtn = By.xpath("//button[contains(@onclick, 'cartManager.toggle')]");
    private final By mobileLogoutBtn = By
        .xpath("//button[normalize-space()='SIGN OUT']");
    private final By desktopLogoutBtn = By
        .xpath("//button[normalize-space()='Sign Out']");
    private final By wishlistBtn = By
        .xpath("//button[contains(@onclick, 'wishlistManager.toggleDrawer')]");

    // Links
    private final By homeLink = By.linkText("HOME");
    private final By storeLink = By.linkText("STORE");
    private final By loginLink = By.linkText("LOGIN");

    public Navbar(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isMobile() {
        try {
            return driver.findElement(hamburgerBtn).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void openMobileMenu() {
        if (isMobile() && !isMobileMenuOpen()) {
            SeleniumUtils.waitAndClick(driver, hamburgerBtn, SeleniumUtils.DEFAULT_TIMEOUT);
            SeleniumUtils.pause(300);
        }
    }

    public void closeMobileMenu() {
        if (isMobile() && isMobileMenuOpen()) {
            SeleniumUtils.waitAndClick(driver, hamburgerBtn, SeleniumUtils.DEFAULT_TIMEOUT);
            SeleniumUtils.pause(300);
        }
    }

    public boolean isMobileMenuOpen() {
        try {
            WebElement menu = driver.findElement(mobileMenu);
            return menu.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAuthenticated() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        if (isMobile()) {
            openMobileMenu();

            return wait.until(d -> {
                WebElement userMenu = d.findElement(mobileUserMenu);
                WebElement authBtns = d.findElement(mobileAuthButtons);

                return userMenu.isDisplayed() && !authBtns.isDisplayed();
            });
        } else {
            return wait.until(d -> {
                List<WebElement> loginLinks = d.findElements(loginLink);
                return loginLinks.isEmpty() || !loginLinks.get(0).isDisplayed();
            });
        }
    }

    public String getUserNameDisplay() {
        if (isMobile()) {
            boolean wasOpen = isMobileMenuOpen();
            openMobileMenu();

            String userName = driver.findElement(mobileUserName).getText().trim();

            if (!wasOpen) {
                closeMobileMenu();
            }
            return userName;
        }

        // Desktop
        return driver.findElement(desktopUserNameDisplay).getText().trim();
    }

    public void clickLogin() {
        if (isMobile()) {
            openMobileMenu();
            driver.findElement(loginLink).click();
        } else {
            driver.findElement(loginLink).click();
        }
    }

    public void clickLogout() {
        if (isMobile()) {
            openMobileMenu();
            SeleniumUtils.pause(500);

            SeleniumUtils.hardWaitAndClick(driver, mobileLogoutBtn, SeleniumUtils.DEFAULT_TIMEOUT);

        } else {
            SeleniumUtils.waitAndClick(driver, userMenuButton, SeleniumUtils.DEFAULT_TIMEOUT);
            SeleniumUtils.hardWaitAndClick(driver, desktopLogoutBtn, SeleniumUtils.DEFAULT_TIMEOUT);
        }
    }

    public boolean areAuthButtonsVisible() {
        if (isMobile()) {
            boolean wasOpen = isMobileMenuOpen();
            openMobileMenu();

            boolean visible;
            try {
                visible = driver.findElement(mobileAuthButtons).isDisplayed();
            } catch (Exception e) {
                visible = false;
            }

            if (!wasOpen) {
                closeMobileMenu();
            }
            return visible;
        }

        try {
            return driver.findElement(desktopAuthButtons).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void goToStore() {
        if (isMobile()) {
            openMobileMenu();
            driver.findElement(storeLink).click();
            closeMobileMenu();
        } else {
            driver.findElement(storeLink).click();
        }
    }

    public void goToHome() {
        if (isMobile()) {
            openMobileMenu();
            driver.findElement(homeLink).click();
            closeMobileMenu();
        } else {
            driver.findElement(homeLink).click();
        }
    }

    public int getCartCount() {
        By locator = isMobile() ? mobileCartCount : desktopCartCount;
        try {
            WebElement count = driver.findElement(locator);
            String text = count.getText().trim();
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getWishlistCount() {
        By locator = isMobile() ? mobileWishlistCount : desktopWishlistCount;
        try {
            WebElement count = driver.findElement(locator);
            String text = count.getText().trim();
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }

    public void openCart() {
        driver.findElement(cartBtn).click();
        SeleniumUtils.pause(500);
    }

    public void openWishlist() {
        driver.findElement(wishlistBtn).click();
        SeleniumUtils.pause(500);
    }

    public boolean isOnHomePage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.equals(SeleniumUtils.BASE_URL + "/") ||
            currentUrl.equals(SeleniumUtils.BASE_URL);
    }

    public boolean isOnStorePage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/store");
    }

    public boolean isOnLoginPage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/login");
    }
}