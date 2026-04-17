package com.ecommerce.lab.automation.pages;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ecommerce.lab.automation.components.Product;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class ProductPage {
    private WebDriver driver;

    // Search
    private final By searchInput = By.id("product-search");
    private final By resultsCount = By.id("results-count");

    // Category
    private final By categoryList = By.id("category-list");

    private By categoryOption(String category) {
        return By
            .xpath("//div[@id='category-list']//div[contains(@onclick, '" + category + "')]");
    }

    // Price
    private final By minThumb = By.id("min-thumb");
    private final By maxThumb = By.id("max-thumb");
    private final By minPriceDisplay = By.id("min-price-display");
    private final By maxPriceDisplay = By.id("max-price-display");

    // Sorting
    private final By sortDropdown = By.id("products-filter");

    // Stock
    private final By inStockFilter = By.id("in-stock-filter");

    // Pagination
    private final By nextBtn = By.id("next-btn");
    private final By prevBtn = By.id("prev-btn");
    private final By pageInfo = By.id("page-info");

    // Product container
    private final By productContainer = By.id("product-list-container");

    private By productButton(String id) {
        return By.xpath("//button[contains(@onclick, 'addItem(" + id + ")')]");
    }

    public ProductPage(WebDriver driver) { this.driver = driver; }

    // ==================== NAVIGATION ====================
    public void navigateTo() {
        SeleniumUtils.navigateTo(driver, "/products");
        waitForPageToLoad();
    }

    private void waitForPageToLoad() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productContainer));
    }

    // ==================== SEARCH ====================
    public void search(String query) {
        WebElement input = driver.findElement(searchInput);
        input.clear();
        input.sendKeys(query);
        SeleniumUtils.pause(500); // Wait for debounce
    }

    public String getResultsCount() { return driver.findElement(resultsCount).getText(); }

    // ==================== CATEGORY ====================
    public void selectCategory(String category) {
        SeleniumUtils.waitAndClick(driver, categoryOption(category), SeleniumUtils.DEFAULT_TIMEOUT);
        SeleniumUtils.pause(500);
    }

    // ==================== PRICE ====================
    public void setMinPrice(int min) {
        SeleniumUtils.navigateTo(driver, "/products?minPrice=" + min);
        waitForPageToLoad();
    }

    public void setMaxPrice(int max) {
        SeleniumUtils.navigateTo(driver, "/products?maxPrice=" + max);
        waitForPageToLoad();
    }

    public void setPriceRange(int min, int max) {
        SeleniumUtils.navigateTo(driver, "/products?minPrice=" + min + "&maxPrice=" + max);
        waitForPageToLoad();
    }

    public String getMinPriceDisplay() { return driver.findElement(minPriceDisplay).getText(); }

    public String getMaxPriceDisplay() { return driver.findElement(maxPriceDisplay).getText(); }

    // ==================== SORTING ====================
    public void sortBy(String option) {
        Select select = new Select(driver.findElement(sortDropdown));
        select.selectByVisibleText(option);
        SeleniumUtils.pause(500);
    }

    // ==================== STOCK ====================
    public void toggleInStockOnly() {
        SeleniumUtils.waitAndClick(driver, inStockFilter, SeleniumUtils.DEFAULT_TIMEOUT);
        SeleniumUtils.pause(500);
    }

    // ==================== PAGINATION ====================
    public void nextPage() {
        SeleniumUtils.waitAndClick(driver, nextBtn, SeleniumUtils.DEFAULT_TIMEOUT);
        SeleniumUtils.pause(500);
    }

    public void previousPage() {
        SeleniumUtils.waitAndClick(driver, prevBtn, SeleniumUtils.DEFAULT_TIMEOUT);
        SeleniumUtils.pause(500);
    }

    public String getPageInfo() { return driver.findElement(pageInfo).getText(); }

    public boolean isNextEnabled() { return driver.findElement(nextBtn).isEnabled(); }

    public boolean isPrevEnabled() { return driver.findElement(prevBtn).isEnabled(); }

    // ==================== PRODUCTS ====================
    public void addProductToCart(String productId) {
        SeleniumUtils.waitAndClick(driver, productButton(productId), SeleniumUtils.DEFAULT_TIMEOUT);
    }

    public List<Product> getProducts() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productContainer));

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
        return getProducts().stream()
            .filter(condition)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public int getProductCount() { return getProducts().size(); }
}