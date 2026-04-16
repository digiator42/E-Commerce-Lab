package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.pages.HomePage;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class HomeSteps {

    private WebDriver getDriver() {
        return DriverManager.getSmartDriver();
    }
    
    private HomePage getHomePage() {
        return new HomePage(getDriver());
    }

    @Given("the user is on the home page")
    public void the_user_is_on_the_home_page() {
        getHomePage().navigateTo();
    }
    
    @Then("the home page should be displayed")
    public void the_home_page_should_be_displayed() {
        assertThat("Home page is not loaded", 
            getHomePage().isPageLoaded(), is(true));
    }
    
    @Then("the hero banner should be visible")
    public void the_hero_banner_should_be_visible() {
        assertThat("Hero banner is not displayed", 
            getHomePage().isHeroBannerDisplayed(), is(true));
    }
    
    @Then("the Ramadan Special badge should be displayed")
    public void the_ramadan_special_badge_should_be_displayed() {
        assertThat("Ramadan Special badge is not displayed", 
            getHomePage().isRamadanSpecialBadgeDisplayed(), is(true));
    }
    
    @When("the user clicks on Shop Ramadan Deals")
    public void the_user_clicks_on_shop_ramadan_deals() {
        getHomePage().clickShopRamadanDeals();
    }
    
    @When("the user clicks on Gift Cards in hero section")
    public void the_user_clicks_on_gift_cards_in_hero_section() {
        getHomePage().clickGiftCardsHero();
    }
    
    @Then("the Gift Card promo should be visible")
    public void the_gift_card_promo_should_be_visible() {
        assertThat("Gift Card promo is not displayed", 
            getHomePage().isGiftCardPromoDisplayed(), is(true));
    }
    
    @Then("the Flash Sale promo should be visible")
    public void the_flash_sale_promo_should_be_visible() {
        assertThat("Flash Sale promo is not displayed", 
            getHomePage().isFlashSalePromoDisplayed(), is(true));
    }
    
    @When("the user clicks on Gift Card promo")
    public void the_user_clicks_on_gift_card_promo() {
        getHomePage().clickGiftCardPromo();
    }
    
    @When("the user clicks on Flash Sale promo")
    public void the_user_clicks_on_flash_sale_promo() {
        getHomePage().clickFlashSalePromo();
    }
    
    @Then("the Electronics section should be visible")
    public void the_electronics_section_should_be_visible() {
        getHomePage().scrollToElectronicsSection();
        assertThat("Electronics section is not displayed", 
            getHomePage().isElectronicsSectionDisplayed(), is(true));
    }
    
    @Then("the Fashion section should be visible")
    public void the_fashion_section_should_be_visible() {
        getHomePage().scrollToFashionSection();
        assertThat("Fashion section is not displayed", 
            getHomePage().isFashionSectionDisplayed(), is(true));
    }
    
    @Then("the Home & Living section should be visible")
    public void the_home_living_section_should_be_visible() {
        getHomePage().scrollToHomeSection();
        assertThat("Home section is not displayed", 
            getHomePage().isHomeSectionDisplayed(), is(true));
    }
    
    @Then("there should be at least {int} products in Electronics section")
    public void there_should_be_at_least_products_in_electronics_section(int minCount) {
        int count = getHomePage().getElectronicsProductCount();
        assertThat("Electronics product count is less than expected", 
            count, greaterThan(minCount - 1));
    }
    
    @Then("there should be at least {int} products in Fashion section")
    public void there_should_be_at_least_products_in_fashion_section(int minCount) {
        int count = getHomePage().getFashionProductCount();
        assertThat("Fashion product count is less than expected", 
            count, greaterThan(minCount - 1));
    }
    
    @Then("there should be at least {int} products in Home section")
    public void there_should_be_at_least_products_in_home_section(int minCount) {
        int count = getHomePage().getHomeProductCount();
        assertThat("Home product count is less than expected", 
            count, greaterThan(minCount - 1));
    }
    
    @Then("the Ramadan banner should be visible")
    public void the_ramadan_banner_should_be_visible() {
        getHomePage().scrollToRamadanBanner();
        assertThat("Ramadan banner is not displayed", 
            getHomePage().isRamadanBannerDisplayed(), is(true));
    }
    
    @Then("the Ramadan promo code should be {string}")
    public void the_ramadan_promo_code_should_be(String expectedCode) {
        String actualCode = getHomePage().getRamadanPromoCode();
        assertThat("Promo code does not match", actualCode, is(expectedCode));
    }
    
    @When("the user adds {string} to cart from home page")
    public void the_user_adds_to_cart_from_home_page(String productName) {
        getHomePage().addProductToCartByName(productName);
    }
    
    @Then("the home page should display products")
    public void the_home_page_should_display_products() {
        assertThat("No products found on home page", 
            getHomePage().hasProducts(), is(true));
        assertThat("Product list is empty", 
            getHomePage().getAllProductNames(), is(not(empty())));
    }
}