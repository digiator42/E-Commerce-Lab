package com.ecommerce.lab.automation.pages;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ecommerce.lab.automation.components.Product;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

public class HomePage {
    private final WebDriver driver;

    // ==================== MAIN CONTAINER ====================
    private final By mainContent = By.id("content");
    
    // ==================== HERO BANNER SECTION ====================
    private final By heroBanner = By.cssSelector(".relative.overflow-hidden.rounded-3xl.bg-gradient-to-r");
    private final By ramadanSpecialBadge = By.xpath("//span[contains(text(), 'Ramadan Special')]");
    private final By heroTitle = By.cssSelector("h1");
    private final By heroDescription = By.cssSelector("p.text-xl, p.text-2xl");
    private final By shopRamadanDealsBtn = By.xpath("//a[contains(text(), 'Shop Ramadan Deals')]");
    private final By giftCardsHeroBtn = By.xpath("//a[contains(text(), 'Gift Cards')]");
    private final By heroImage = By.cssSelector("img[alt='Ramadan Special']");
    
    // ==================== PROMO CARDS SECTION ====================
    private final By giftCardPromo = By.xpath("//div[contains(@class, 'from-purple-500')]");
    private final By flashSalePromo = By.xpath("//div[contains(@class, 'from-amber-500')]");
    private final By giftCardTitle = By.xpath("//h3[contains(text(), 'Gift Cards')]");
    private final By flashSaleTitle = By.xpath("//h3[contains(text(), 'Flash Sale')]");
    private final By giftCardPriceRange = By.xpath("//div[contains(@class, 'from-purple-500')]//span[contains(text(), '$25')]/ancestor::div[contains(@class, 'flex')]");
    private final By flashSaleDiscount = By.xpath("//div[contains(@class, 'from-amber-500')]//span[contains(text(), '60%')]");
    private final By sendGiftBtn = By.xpath("//button[contains(text(), 'Send a Gift')]");
    private final By shopNowFlashSaleBtn = By.xpath("//button[contains(text(), 'Shop Now')]");
    
    // ==================== CATEGORY SECTIONS ====================
    // Section Headers
    private final By electronicsSection = By.id("electronics-products");
    private final By fashionSection = By.id("fashion-products");
    private final By homeSection = By.id("home-products");
    
    private final By electronicsHeader = By.xpath("//h2[contains(text(), 'Electronics & Gadgets')]");
    private final By fashionHeader = By.xpath("//h2[contains(text(), 'Fashion & Apparel')]");
    private final By homeHeader = By.xpath("//h2[contains(text(), 'Home & Living')]");
    
    // View All Links
    private final By electronicsViewAll = By.xpath("//a[contains(@href, '/products?category=Electronics')]");
    private final By fashionViewAll = By.xpath("//a[contains(@href, '/products?category=Clothing')]");
    private final By homeViewAll = By.xpath("//a[contains(@href, '/products?category=Home+%26+Garden')]");
    
    // Category section containers (for product grid)
    private final By electronicsProductGrid = By.cssSelector("#electronics-products");
    private final By fashionProductGrid = By.cssSelector("#fashion-products");
    private final By homeProductGrid = By.cssSelector("#home-products");
    
    // Individual product cards within sections
    private final By productCards = By.cssSelector(".group.bg-white.rounded-2xl");
    private final By electronicsProducts = By.cssSelector("#electronics-products .group.bg-white.rounded-2xl");
    private final By fashionProducts = By.cssSelector("#fashion-products .group.bg-white.rounded-2xl");
    private final By homeProducts = By.cssSelector("#home-products .group.bg-white.rounded-2xl");
    
    // ==================== RAMADAN BANNER SECTION ====================
    private final By ramadanBanner = By.xpath("//div[contains(@class, 'from-emerald-800')]");
    private final By ramadanLastDaysBadge = By.xpath("//span[contains(text(), 'Last Days of Ramadan')]");
    private final By ramadanBannerTitle = By.xpath("//h2[contains(text(), 'Extra 20% Off')]");
    private final By ramadanPromoCode = By.xpath("//span[contains(text(), 'RAMADAN20')]");
    private final By shopAllDealsBtn = By.xpath("//a[contains(text(), 'Shop All Deals')]");
    
    // ==================== NEW BADGE (on products) ====================
    private final By newBadge = By.xpath("//span[contains(text(), 'NEW')]");
    private final By guestBadge = By.xpath("//span[contains(text(), 'GUEST')]");

    // ==================== CONSTRUCTOR ====================
    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    // ==================== NAVIGATION METHODS ====================
    
    /**
     * Navigate to home page
     */
    public void navigateTo() {
        SeleniumUtils.navigateTo(driver, "/");
        waitForPageToLoad();
    }
    
    /**
     * Check if home page is loaded
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
        SeleniumUtils.pause(1000); // Additional wait for dynamic content
    }

    // ==================== HERO BANNER METHODS ====================
    
    /**
     * Check if hero banner is displayed
     */
    public boolean isHeroBannerDisplayed() {
        return driver.findElement(heroBanner).isDisplayed();
    }
    
    /**
     * Get hero banner title text
     */
    public String getHeroTitle() {
        return driver.findElement(heroTitle).getText();
    }
    
    /**
     * Get hero description text
     */
    public String getHeroDescription() {
        return driver.findElement(heroDescription).getText();
    }
    
    /**
     * Click on Shop Ramadan Deals button
     */
    public void clickShopRamadanDeals() {
        SeleniumUtils.waitAndClick(driver, shopRamadanDealsBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Click on Gift Cards button in hero section
     */
    public void clickGiftCardsHero() {
        SeleniumUtils.waitAndClick(driver, giftCardsHeroBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Check if Ramadan Special badge is displayed
     */
    public boolean isRamadanSpecialBadgeDisplayed() {
        return driver.findElement(ramadanSpecialBadge).isDisplayed();
    }
    
    /**
     * Get hero image source
     */
    public String getHeroImageSrc() {
        return driver.findElement(heroImage).getAttribute("src");
    }

    // ==================== PROMO CARDS METHODS ====================
    
    /**
     * Check if Gift Card promo is displayed
     */
    public boolean isGiftCardPromoDisplayed() {
        return driver.findElement(giftCardPromo).isDisplayed();
    }
    
    /**
     * Check if Flash Sale promo is displayed
     */
    public boolean isFlashSalePromoDisplayed() {
        return driver.findElement(flashSalePromo).isDisplayed();
    }
    
    /**
     * Click on Gift Card promo
     */
    public void clickGiftCardPromo() {
        driver.findElement(giftCardPromo).click();
    }
    
    /**
     * Click on Flash Sale promo
     */
    public void clickFlashSalePromo() {
        driver.findElement(flashSalePromo).click();
    }
    
    /**
     * Click Send a Gift button
     */
    public void clickSendGift() {
        SeleniumUtils.waitAndClick(driver, sendGiftBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Click Shop Now button in Flash Sale
     */
    public void clickShopNowFlashSale() {
        SeleniumUtils.waitAndClick(driver, shopNowFlashSaleBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Get Gift Card price range text
     */
    public String getGiftCardPriceRange() {
        return driver.findElement(giftCardPriceRange).getText();
    }
    
    /**
     * Get Flash Sale discount text
     */
    public String getFlashSaleDiscount() {
        return driver.findElement(flashSaleDiscount).getText();
    }

    // ==================== CATEGORY SECTION METHODS ====================
    
    /**
     * Check if Electronics section is displayed
     */
    public boolean isElectronicsSectionDisplayed() {
        return driver.findElement(electronicsSection).isDisplayed();
    }
    
    /**
     * Check if Fashion section is displayed
     */
    public boolean isFashionSectionDisplayed() {
        return driver.findElement(fashionSection).isDisplayed();
    }
    
    /**
     * Check if Home section is displayed
     */
    public boolean isHomeSectionDisplayed() {
        return driver.findElement(homeSection).isDisplayed();
    }
    
    /**
     * Get Electronics section header text
     */
    public String getElectronicsHeader() {
        return driver.findElement(electronicsHeader).getText();
    }
    
    /**
     * Get Fashion section header text
     */
    public String getFashionHeader() {
        return driver.findElement(fashionHeader).getText();
    }
    
    /**
     * Get Home section header text
     */
    public String getHomeHeader() {
        return driver.findElement(homeHeader).getText();
    }
    
    /**
     * Click View All for Electronics
     */
    public void clickElectronicsViewAll() {
        SeleniumUtils.waitAndClick(driver, electronicsViewAll, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Click View All for Fashion
     */
    public void clickFashionViewAll() {
        SeleniumUtils.waitAndClick(driver, fashionViewAll, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Click View All for Home
     */
    public void clickHomeViewAll() {
        SeleniumUtils.waitAndClick(driver, homeViewAll, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    // ==================== PRODUCT GRID METHODS ====================
    
    /**
     * Get all products from Electronics section
     */
    public List<Product> getElectronicsProducts() {
        List<WebElement> productElements = SeleniumUtils.waitForAllElements(
            driver,
            electronicsProducts,
            SeleniumUtils.DEFAULT_TIMEOUT
        );
        return productElements.stream().map(Product::new).toList();
    }
    
    /**
     * Get all products from Fashion section
     */
    public List<Product> getFashionProducts() {
        List<WebElement> productElements = SeleniumUtils.waitForAllElements(
            driver,
            fashionProducts,
            SeleniumUtils.DEFAULT_TIMEOUT
        );
        return productElements.stream().map(Product::new).toList();
    }
    
    /**
     * Get all products from Home section
     */
    public List<Product> getHomeProducts() {
        List<WebElement> productElements = SeleniumUtils.waitForAllElements(
            driver,
            homeProducts,
            SeleniumUtils.DEFAULT_TIMEOUT
        );
        return productElements.stream().map(Product::new).toList();
    }
    
    /**
     * Get all products from all sections
     */
    public List<Product> getAllProducts() {
        List<WebElement> productElements = SeleniumUtils.waitForAllElements(
            driver,
            productCards,
            SeleniumUtils.DEFAULT_TIMEOUT
        );
        return productElements.stream().map(Product::new).toList();
    }
    
    /**
     * Get product count in Electronics section
     */
    public int getElectronicsProductCount() {
        return getElectronicsProducts().size();
    }
    
    /**
     * Get product count in Fashion section
     */
    public int getFashionProductCount() {
        return getFashionProducts().size();
    }
    
    /**
     * Get product count in Home section
     */
    public int getHomeProductCount() {
        return getHomeProducts().size();
    }
    
    /**
     * Get total product count across all sections
     */
    public int getTotalProductCount() {
        return getAllProducts().size();
    }
    
    /**
     * Find a product by name in any section
     */
    public Optional<Product> findProductByName(String productName) {
        return getAllProducts().stream()
            .filter(p -> p.getName().trim().equalsIgnoreCase(productName))
            .findFirst();
    }
    
    /**
     * Find a product by ID in any section
     */
    public Optional<Product> findProductById(int productId) {
        return getAllProducts().stream()
            .filter(p -> p.getProductId() == productId)
            .findFirst();
    }
    
    /**
     * Add product to cart by name
     */
    public void addProductToCartByName(String productName) {
        findProductByName(productName)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productName))
            .addToCart();
    }
    
    /**
     * Add product to cart by ID
     */
    public void addProductToCartById(int productId) {
        findProductById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId))
            .addToCart();
    }
    
    /**
     * Toggle wishlist for product by name
     */
    public void toggleWishlistByName(String productName) {
        findProductByName(productName)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productName))
            .toggleWishlist();
    }

    // ==================== RAMADAN BANNER METHODS ====================
    
    /**
     * Check if Ramadan banner is displayed
     */
    public boolean isRamadanBannerDisplayed() {
        return driver.findElement(ramadanBanner).isDisplayed();
    }
    
    /**
     * Get Ramadan banner title
     */
    public String getRamadanBannerTitle() {
        return driver.findElement(ramadanBannerTitle).getText();
    }
    
    /**
     * Get Ramadan promo code
     */
    public String getRamadanPromoCode() {
        return driver.findElement(ramadanPromoCode).getText();
    }
    
    /**
     * Check if Last Days of Ramadan badge is displayed
     */
    public boolean isLastDaysBadgeDisplayed() {
        return driver.findElement(ramadanLastDaysBadge).isDisplayed();
    }
    
    /**
     * Click Shop All Deals button
     */
    public void clickShopAllDeals() {
        SeleniumUtils.waitAndClick(driver, shopAllDealsBtn, SeleniumUtils.DEFAULT_TIMEOUT);
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Scroll to Electronics section
     */
    public void scrollToElectronicsSection() {
        WebElement element = driver.findElement(electronicsHeader);
        SeleniumUtils.scrollToElement(driver, electronicsHeader, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Scroll to Fashion section
     */
    public void scrollToFashionSection() {
        SeleniumUtils.scrollToElement(driver, fashionHeader, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Scroll to Home section
     */
    public void scrollToHomeSection() {
        SeleniumUtils.scrollToElement(driver, homeHeader, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Scroll to Ramadan banner
     */
    public void scrollToRamadanBanner() {
        SeleniumUtils.scrollToElement(driver, ramadanBanner, SeleniumUtils.DEFAULT_TIMEOUT);
    }
    
    /**
     * Check if page has any products
     */
    public boolean hasProducts() {
        return getTotalProductCount() > 0;
    }
    
    /**
     * Get all product names from the page
     */
    public List<String> getAllProductNames() {
        return getAllProducts().stream()
            .map(Product::getName)
            .toList();
    }
    
    /**
     * Get all product prices from the page
     */
    public List<Double> getAllProductPrices() {
        return getAllProducts().stream()
            .map(Product::getPriceValue)
            .toList();
    }
    
    /**
     * Get all product categories from the page
     */
    public List<String> getAllProductCategories() {
        return getAllProducts().stream()
            .map(Product::getCategoryValue)
            .toList();
    }
}