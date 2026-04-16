package com.ecommerce.lab.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class ProfilePage {
    private final WebDriver driver;

    // ==================== MAIN CONTAINER ====================
    private final By mainContent = By.id("content");
    private final By pageTitle = By.cssSelector("h1");
    private final By pageSubtitle = By.cssSelector("p.text-gray-500");

    // ==================== SIDEBAR - PROFILE CARD ====================
    private final By profileSidebar = By.cssSelector(".bg-white.rounded-2xl");
    private final By profilePic = By.id("profile-pic");
    private final By profileInitials = By.id("profile-initials");
    private final By profileDisplayName = By.id("profile-display-name");
    private final By profileEmail = By.id("profile-email");
    private final By lastLoginTime = By.id("last-login-time");
    private final By lastLoginTooltip = By.id("last-login-time-tooltip");

    // ==================== SIDEBAR - NAVIGATION TABS ====================
    private final By tabPersonalInfo = By.xpath("//button[@data-tab='info']");
    private final By tabShippingAddress = By.xpath("//button[@data-tab='address']");
    private final By tabSecurity = By.xpath("//button[@data-tab='security']");
    private final By tab2FA = By.xpath("//button[@data-tab='2fa']");
    private final By tabStoreBalance = By.xpath("//button[@data-tab='redeem']");

    // Generic tab button by data-tab attribute
    private By tabButton(String tabName) {
        return By.xpath("//button[@data-tab='" + tabName + "']");
    }

    // Active tab locator
    private final By activeTab = By.cssSelector(".profile-tab.active");

    // ==================== TAB CONTENT CONTAINERS ====================
    private final By tabInfoContent = By.id("tab-info");
    private final By tabAddressContent = By.id("tab-address");
    private final By tabSecurityContent = By.id("tab-security");
    private final By tab2FAContent = By.id("tab-2fa");
    private final By tabRedeemContent = By.id("tab-redeem");

    // ==================== PERSONAL INFO TAB ====================
    private final By infoForm = By.id("profile-info-form");
    private final By displayNameInput = By.id("display-name");
    private final By profileEmailInput = By.id("profile-email-input");
    private final By infoSubmitBtn = By.cssSelector("#profile-info-form button[type='submit']");

    // ==================== SHIPPING ADDRESS TAB ====================
    private final By addressForm = By.id("profile-address-form");
    private final By streetInput = By.id("address-street");
    private final By cityInput = By.id("address-city");
    private final By stateInput = By.id("address-state");
    private final By zipInput = By.id("address-zip");
    private final By countryInput = By.id("address-country");
    private final By addressSubmitBtn = By
        .cssSelector("#profile-address-form button[type='submit']");

    // ==================== SECURITY TAB ====================
    private final By passwordForm = By.id("profile-password-form");
    private final By currentPasswordInput = By.id("current-password");
    private final By newPasswordInput = By.id("new-password");
    private final By confirmPasswordInput = By.id("confirm-password");
    private final By passwordSubmitBtn = By
        .cssSelector("#profile-password-form button[type='submit']");

    // ==================== 2FA TAB ====================
    private final By twoFAToggle = By.id("2fa-toggle");
    private final By twoFAStatus = By.id("2fa-status");

    // ==================== STORE BALANCE / REDEEM TAB ====================
    private final By storeBalanceDisplay = By.id("store-balance-display");
    private final By redeemForm = By.id("redeem-giftcard-form");
    private final By giftCardCodeInput = By.id("giftcard-code");
    private final By redeemBtn = By.id("redeem-btn");
    private final By redeemHistory = By.id("redeem-history");

    // ==================== CONSTRUCTOR ====================
    public ProfilePage(WebDriver driver) { this.driver = driver; }

    // ==================== NAVIGATION METHODS ====================

    /**
     * Navigate to profile page
     */
    public void navigateTo() {
        SeleniumUtils.navigateTo(driver, "/profile");
        waitForPageToLoad();
    }

    /**
     * Check if profile page is loaded
     */
    public boolean isPageLoaded() {
        return SeleniumUtils.waitForElement(driver, mainContent, SeleniumUtils.DEFAULT_TIMEOUT)
            .isDisplayed();
    }

    /**
     * Wait for page to fully load
     */
    private void waitForPageToLoad() {
        SeleniumUtils.waitForElement(driver, mainContent, SeleniumUtils.DEFAULT_TIMEOUT);
        SeleniumUtils.pause(1000);
    }

    /**
     * Get page title
     */
    public String getPageTitle() { return driver.findElement(pageTitle).getText(); }

    /**
     * Get page subtitle
     */
    public String getPageSubtitle() { return driver.findElement(pageSubtitle).getText(); }

    // ==================== PROFILE SIDEBAR METHODS ====================

    /**
     * Get profile display name
     */
    public String getProfileDisplayName() {
        return driver.findElement(profileDisplayName).getText();
    }

    /**
     * Get profile email
     */
    public String getProfileEmail() { return driver.findElement(profileEmail).getText(); }

    /**
     * Get profile initials
     */
    public String getProfileInitials() { return driver.findElement(profileInitials).getText(); }

    /**
     * Get last login time
     */
    public String getLastLoginTime() { return driver.findElement(lastLoginTime).getText(); }

    /**
     * Hover over last login time to show tooltip
     */
    public void hoverOverLastLoginTime() {
        WebElement element = driver.findElement(lastLoginTime);
        SeleniumUtils.scrollToElement(driver, lastLoginTime, SeleniumUtils.DEFAULT_TIMEOUT);
        // Tooltip should appear on hover
    }

    /**
     * Check if profile picture is displayed
     */
    public boolean isProfilePicDisplayed() {
        return driver.findElement(profilePic).isDisplayed();
    }

    // ==================== TAB NAVIGATION METHODS ====================

    /**
     * Click on Personal Info tab
     */
    public void clickPersonalInfoTab() {
        SeleniumUtils.waitAndClick(driver, tabPersonalInfo, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Click on Shipping Address tab
     */
    public void clickShippingAddressTab() {
        SeleniumUtils.waitAndClick(driver, tabShippingAddress, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Click on Security tab
     */
    public void clickSecurityTab() {
        SeleniumUtils.waitAndClick(driver, tabSecurity, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Click on 2FA Settings tab
     */
    public void click2FATab() {
        SeleniumUtils.waitAndClick(driver, tab2FA, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Click on Store Balance tab
     */
    public void clickStoreBalanceTab() {
        SeleniumUtils.waitAndClick(driver, tabStoreBalance, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Click tab by name
     */
    public void clickTab(String tabName) {
        SeleniumUtils.waitAndClick(driver, tabButton(tabName), SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Get currently active tab name
     */
    public String getActiveTabName() {
        WebElement active = driver.findElement(activeTab);
        return active.getAttribute("data-tab");
    }

    /**
     * Check if Personal Info tab is visible
     */
    public boolean isPersonalInfoTabVisible() {
        return !driver.findElement(tabInfoContent).getAttribute("class").contains("hidden");
    }

    /**
     * Check if Shipping Address tab is visible
     */
    public boolean isShippingAddressTabVisible() {
        return !driver.findElement(tabAddressContent).getAttribute("class").contains("hidden");
    }

    /**
     * Check if Security tab is visible
     */
    public boolean isSecurityTabVisible() {
        return !driver.findElement(tabSecurityContent).getAttribute("class").contains("hidden");
    }

    /**
     * Check if 2FA tab is visible
     */
    public boolean is2FATabVisible() {
        return !driver.findElement(tab2FAContent).getAttribute("class").contains("hidden");
    }

    /**
     * Check if Store Balance tab is visible
     */
    public boolean isStoreBalanceTabVisible() {
        return !driver.findElement(tabRedeemContent).getAttribute("class").contains("hidden");
    }

    // ==================== PERSONAL INFO TAB METHODS ====================

    /**
     * Get current display name value
     */
    public String getDisplayNameValue() {
        return driver.findElement(displayNameInput).getAttribute("value");
    }

    /**
     * Get current email value (readonly)
     */
    public String getProfileEmailInputValue() {
        return driver.findElement(profileEmailInput).getAttribute("value");
    }

    /**
     * Check if email input is readonly
     */
    public boolean isEmailInputReadonly() {
        WebElement emailInput = driver.findElement(profileEmailInput);
        return emailInput.getAttribute("readonly") != null ||
            emailInput.getAttribute("disabled") != null;
    }

    /**
     * Enter display name
     */
    public void enterDisplayName(String displayName) {
        WebElement input = driver.findElement(displayNameInput);
        input.clear();
        input.sendKeys(displayName);
    }

    /**
     * Submit personal info form
     */
    public void submitPersonalInfo() {
        SeleniumUtils.waitAndClick(driver, infoSubmitBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Update display name
     */
    public void updateDisplayName(String newDisplayName) {
        enterDisplayName(newDisplayName);
        submitPersonalInfo();
    }

    /**
     * Check if personal info form is displayed
     */
    public boolean isPersonalInfoFormDisplayed() {
        return driver.findElement(infoForm).isDisplayed();
    }

    // ==================== SHIPPING ADDRESS TAB METHODS ====================

    /**
     * Get street address value
     */
    public String getStreetValue() {
        return driver.findElement(streetInput).getAttribute("value");
    }

    /**
     * Get city value
     */
    public String getCityValue() { return driver.findElement(cityInput).getAttribute("value"); }

    /**
     * Get state value
     */
    public String getStateValue() {
        return driver.findElement(stateInput).getAttribute("value");
    }

    /**
     * Get ZIP code value
     */
    public String getZipValue() { return driver.findElement(zipInput).getAttribute("value"); }

    /**
     * Get country value
     */
    public String getCountryValue() {
        return driver.findElement(countryInput).getAttribute("value");
    }

    /**
     * Enter street address
     */
    public void enterStreet(String street) {
        WebElement input = driver.findElement(streetInput);
        input.clear();
        input.sendKeys(street);
    }

    /**
     * Enter city
     */
    public void enterCity(String city) {
        WebElement input = driver.findElement(cityInput);
        input.clear();
        input.sendKeys(city);
    }

    /**
     * Enter state
     */
    public void enterState(String state) {
        WebElement input = driver.findElement(stateInput);
        input.clear();
        input.sendKeys(state);
    }

    /**
     * Enter ZIP code
     */
    public void enterZip(String zip) {
        WebElement input = driver.findElement(zipInput);
        input.clear();
        input.sendKeys(zip);
    }

    /**
     * Enter country
     */
    public void enterCountry(String country) {
        WebElement input = driver.findElement(countryInput);
        input.clear();
        input.sendKeys(country);
    }

    /**
     * Fill complete address
     */
    public void fillAddress(String street, String city, String state, String zip, String country) {
        enterStreet(street);
        enterCity(city);
        enterState(state);
        enterZip(zip);
        enterCountry(country);
    }

    /**
     * Submit address form
     */
    public void submitAddress() {
        SeleniumUtils.waitAndClick(driver, addressSubmitBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Check if address form is displayed
     */
    public boolean isAddressFormDisplayed() {
        return driver.findElement(addressForm).isDisplayed();
    }

    /**
     * Clear all address fields
     */
    public void clearAddressFields() {
        driver.findElement(streetInput).clear();
        driver.findElement(cityInput).clear();
        driver.findElement(stateInput).clear();
        driver.findElement(zipInput).clear();
        driver.findElement(countryInput).clear();
    }

    // ==================== SECURITY TAB METHODS ====================

    /**
     * Enter current password
     */
    public void enterCurrentPassword(String password) {
        WebElement input = driver.findElement(currentPasswordInput);
        input.clear();
        input.sendKeys(password);
    }

    /**
     * Enter new password
     */
    public void enterNewPassword(String password) {
        WebElement input = driver.findElement(newPasswordInput);
        input.clear();
        input.sendKeys(password);
    }

    /**
     * Enter confirm password
     */
    public void enterConfirmPassword(String password) {
        WebElement input = driver.findElement(confirmPasswordInput);
        input.clear();
        input.sendKeys(password);
    }

    /**
     * Fill password change form
     */
    public void fillPasswordChange(
        String currentPassword,
        String newPassword,
        String confirmPassword
    ) {
        enterCurrentPassword(currentPassword);
        enterNewPassword(newPassword);
        enterConfirmPassword(confirmPassword);
    }

    /**
     * Submit password change
     */
    public void submitPasswordChange() {
        SeleniumUtils.waitAndClick(driver, passwordSubmitBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Change password
     */
    public void changePassword(String currentPassword, String newPassword) {
        fillPasswordChange(currentPassword, newPassword, newPassword);
        submitPasswordChange();
    }

    /**
     * Check if password form is displayed
     */
    public boolean isPasswordFormDisplayed() {
        return driver.findElement(passwordForm).isDisplayed();
    }

    /**
     * Get current password field type (should be password)
     */
    public String getCurrentPasswordFieldType() {
        return driver.findElement(currentPasswordInput).getAttribute("type");
    }

    /**
     * Get new password field type
     */
    public String getNewPasswordFieldType() {
        return driver.findElement(newPasswordInput).getAttribute("type");
    }

    // ==================== 2FA TAB METHODS ====================

    /**
     * Check if 2FA is enabled
     */
    public boolean is2FAEnabled() {
        WebElement toggle = driver.findElement(twoFAToggle);
        return toggle.isSelected();
    }

    /**
     * Toggle 2FA
     */
    public void toggle2FA() {
        SeleniumUtils.waitAndClickJS(driver, twoFAToggle, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Enable 2FA
     */
    public void enable2FA() {
        if (!is2FAEnabled()) {
            toggle2FA();
        }
    }

    /**
     * Disable 2FA
     */
    public void disable2FA() {
        if (is2FAEnabled()) {
            toggle2FA();
        }
    }

    /**
     * Get 2FA status text
     */
    public String get2FAStatusText() { return driver.findElement(twoFAStatus).getText(); }

    /**
     * Check if 2FA status contains text
     */
    public boolean is2FADisabled() {
        return get2FAStatusText().toLowerCase().contains("disabled");
    }

    /**
     * Check if 2FA status indicates enabled
     */
    public boolean is2FAEnabledStatus() {
        return get2FAStatusText().toLowerCase().contains("enabled");
    }

    // ==================== STORE BALANCE / REDEEM TAB METHODS ====================

    /**
     * Get store balance amount
     */
    public String getStoreBalance() { return driver.findElement(storeBalanceDisplay).getText(); }

    /**
     * Get store balance as double
     */
    public double getStoreBalanceValue() {
        String balanceText = getStoreBalance();
        return Double.parseDouble(balanceText.replaceAll("[^0-9.]", ""));
    }

    /**
     * Enter gift card code
     */
    public void enterGiftCardCode(String code) {
        WebElement input = driver.findElement(giftCardCodeInput);
        input.clear();
        input.sendKeys(code);
    }

    /**
     * Get gift card code value
     */
    public String getGiftCardCodeValue() {
        return driver.findElement(giftCardCodeInput).getAttribute("value");
    }

    /**
     * Click redeem button
     */
    public void clickRedeem() {
        SeleniumUtils.waitAndClick(driver, redeemBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Redeem gift card
     */
    public void redeemGiftCard(String code) {
        enterGiftCardCode(code);
        clickRedeem();
    }

    /**
     * Check if redeem form is displayed
     */
    public boolean isRedeemFormDisplayed() {
        return driver.findElement(redeemForm).isDisplayed();
    }

    /**
     * Check if redeem button is enabled
     */
    public boolean isRedeemButtonEnabled() { return driver.findElement(redeemBtn).isEnabled(); }

    /**
     * Get redeem history items
     */
    public List<WebElement> getRedeemHistoryItems() {
        return driver.findElement(redeemHistory)
            .findElements(By.cssSelector(".space-y-3 > div"));
    }

    /**
     * Check if redeem history has items
     */
    public boolean hasRedeemHistory() {
        List<WebElement> items = getRedeemHistoryItems();
        String historyText = driver.findElement(redeemHistory).getText();
        return !historyText.contains("No recent activity");
    }

    /**
     * Get redeem history text
     */
    public String getRedeemHistoryText() { return driver.findElement(redeemHistory).getText(); }

    /**
     * Clear gift card code input
     */
    public void clearGiftCardCode() { driver.findElement(giftCardCodeInput).clear(); }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if all tabs are present
     */
    public boolean areAllTabsPresent() {
        try {
            driver.findElement(tabPersonalInfo);
            driver.findElement(tabShippingAddress);
            driver.findElement(tabSecurity);
            driver.findElement(tab2FA);
            driver.findElement(tabStoreBalance);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get count of profile tabs
     */
    public int getProfileTabCount() {
        return driver.findElements(By.cssSelector(".profile-tab")).size();
    }

    /**
     * Scroll to redeem form
     */
    public void scrollToRedeemForm() {
        SeleniumUtils.scrollToElement(driver, redeemForm, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Scroll to address form
     */
    public void scrollToAddressForm() {
        SeleniumUtils.scrollToElement(driver, addressForm, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    /**
     * Scroll to password form
     */
    public void scrollToPasswordForm() {
        SeleniumUtils.scrollToElement(driver, passwordForm, SeleniumUtils.DEFAULT_TIMEOUT);
    }
}