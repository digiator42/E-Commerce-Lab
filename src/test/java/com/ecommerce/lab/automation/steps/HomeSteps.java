package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.pages.HomePage;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class HomeSteps {

    private WebDriver getDriver() { return DriverManager.getSmartDriver(); }

    private HomePage getHomePage() { return new HomePage(getDriver()); }

    @Given("the user is on the home page")
    public void the_user_is_on_the_home_page() { getHomePage().navigateTo(); }

    @Then("the home page should be displayed")
    public void the_home_page_should_be_displayed() {
        assertThat(
            "Home page is not loaded",
            getHomePage().isPageLoaded(), is(true)
        );
    }

    @Then("the hero banner should be visible")
    public void the_hero_banner_should_be_visible() {
        assertThat(
            "Hero banner is not displayed",
            getHomePage().isHeroBannerDisplayed(), is(true)
        );
    }

    @Then("the Ramadan Special badge should be displayed")
    public void the_ramadan_special_badge_should_be_displayed() {
        assertThat(
            "Ramadan Special badge is not displayed",
            getHomePage().isRamadanSpecialBadgeDisplayed(), is(true)
        );
    }

    @When("the user clicks on Shop Ramadan Deals")
    public void the_user_clicks_on_shop_ramadan_deals() { getHomePage().clickShopRamadanDeals(); }

    @When("the user clicks on Gift Cards in hero section")
    public void the_user_clicks_on_gift_cards_in_hero_section() {
        getHomePage().clickGiftCardsHero();
    }

    @Then("the Gift Card promo should be visible")
    public void the_gift_card_promo_should_be_visible() {
        assertThat(
            "Gift Card promo is not displayed",
            getHomePage().isGiftCardPromoDisplayed(), is(true)
        );
    }

    @Then("the Flash Sale promo should be visible")
    public void the_flash_sale_promo_should_be_visible() {
        assertThat(
            "Flash Sale promo is not displayed",
            getHomePage().isFlashSalePromoDisplayed(), is(true)
        );
    }

    @When("the user clicks on Gift Card promo")
    public void the_user_clicks_on_gift_card_promo() { getHomePage().clickGiftCardPromo(); }

    @When("the user clicks on Flash Sale promo")
    public void the_user_clicks_on_flash_sale_promo() { getHomePage().clickFlashSalePromo(); }

    @Then("the Electronics section should be visible")
    public void the_electronics_section_should_be_visible() {
        getHomePage().scrollToElectronicsSection();
        assertThat(
            "Electronics section is not displayed",
            getHomePage().isElectronicsSectionDisplayed(), is(true)
        );
    }

    @Then("the Fashion section should be visible")
    public void the_fashion_section_should_be_visible() {
        getHomePage().scrollToFashionSection();
        assertThat(
            "Fashion section is not displayed",
            getHomePage().isFashionSectionDisplayed(), is(true)
        );
    }

    @Then("the Home & Living section should be visible")
    public void the_home_living_section_should_be_visible() {
        getHomePage().scrollToHomeSection();
        assertThat(
            "Home section is not displayed",
            getHomePage().isHomeSectionDisplayed(), is(true)
        );
    }

    @Then("there should be at least {int} products in Electronics section")
    public void there_should_be_at_least_products_in_electronics_section(int minCount) {
        int count = getHomePage().getElectronicsProductCount();
        assertThat(
            "Electronics product count is less than expected",
            count, greaterThan(minCount - 1)
        );
    }

    @Then("there should be at least {int} products in Fashion section")
    public void there_should_be_at_least_products_in_fashion_section(int minCount) {
        int count = getHomePage().getFashionProductCount();
        assertThat(
            "Fashion product count is less than expected",
            count, greaterThan(minCount - 1)
        );
    }

    @Then("there should be at least {int} products in Home section")
    public void there_should_be_at_least_products_in_home_section(int minCount) {
        int count = getHomePage().getHomeProductCount();
        assertThat(
            "Home product count is less than expected",
            count, greaterThan(minCount - 1)
        );
    }

    @Then("the Ramadan banner should be visible")
    public void the_ramadan_banner_should_be_visible() {
        getHomePage().scrollToRamadanBanner();
        assertThat(
            "Ramadan banner is not displayed",
            getHomePage().isRamadanBannerDisplayed(), is(true)
        );
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
        assertThat(
            "No products found on home page",
            getHomePage().hasProducts(), is(true)
        );
        assertThat(
            "Product list is empty",
            getHomePage().getAllProductNames(), is(not(empty()))
        );
    }

    // Add these steps to HomeSteps.java

    @Then("the hero title should contain {string}")
    public void the_hero_title_should_contain(String expectedText) {
        String actualTitle = getHomePage().getHeroTitle();
        assertThat(
            "Hero title does not contain expected text",
            actualTitle, containsString(expectedText)
        );
    }

    @Then("the gift card price range should be {string}")
    public void the_gift_card_price_range_should_be(String expectedRange) {
        String actualRange = getHomePage().getGiftCardPriceRangeText();
        assertThat(
            "Gift card price range mismatch",
            actualRange, containsString(expectedRange)
        );
    }

    @Then("the flash sale discount should be {string}")
    public void the_flash_sale_discount_should_be(String expectedDiscount) {
        String actualDiscount = getHomePage().getFlashSaleDiscountText();
        assertThat(
            "Flash sale discount mismatch",
            actualDiscount, containsString(expectedDiscount)
        );
    }

    @Then("the Ramadan banner title should be {string}")
    public void the_ramadan_banner_title_should_be(String expectedTitle) {
        String actualTitle = getHomePage().getRamadanBannerTitle();
        assertThat(
            "Ramadan banner title mismatch",
            actualTitle, is(expectedTitle)
        );
    }

    @Then("the Last Days of Ramadan badge should be displayed")
    public void the_last_days_of_ramadan_badge_should_be_displayed() {
        assertThat(
            "Last Days of Ramadan badge is not displayed",
            getHomePage().isLastDaysBadgeDisplayed(), is(true)
        );
    }

    @Then("there should be at least {int} total products")
    public void there_should_be_at_least_total_products(int minCount) {
        int count = getHomePage().getTotalProductCount();
        assertThat(
            "Total product count is less than expected",
            count, greaterThanOrEqualTo(minCount)
        );
    }

    @When("the user clicks on Electronics View All")
    public void the_user_clicks_on_electronics_view_all() {
        getHomePage().clickElectronicsViewAll();
    }

    @When("the user clicks on Fashion View All")
    public void the_user_clicks_on_fashion_view_all() { getHomePage().clickFashionViewAll(); }

    @When("the user clicks on Home View All")
    public void the_user_clicks_on_home_view_all() { getHomePage().clickHomeViewAll(); }

    @Then("the user should be on the {string} page")
    public void the_user_should_be_on_the_page(String page) {
        String currentUrl = getDriver().getCurrentUrl();
        assertThat(
            "User is not on the expected page",
            currentUrl, containsString(page)
        );
    }

    @When("the user clicks on {string}")
    public void the_user_clicks_on(String element) {
        switch (element) {
        case "Shop Ramadan Deals":
            getHomePage().clickShopRamadanDeals();
            break;
        case "Gift Cards in hero":
            getHomePage().clickGiftCardsHero();
            break;
        case "Electronics View All":
            getHomePage().clickElectronicsViewAll();
            break;
        case "Fashion View All":
            getHomePage().clickFashionViewAll();
            break;
        case "Home View All":
            getHomePage().clickHomeViewAll();
            break;
        case "Gift Card promo":
            getHomePage().clickGiftCardPromo();
            break;
        case "Flash Sale promo":
            getHomePage().clickFlashSalePromo();
            break;
        case "Shop All Deals":
            getHomePage().clickShopAllDeals();
            break;
        default:
            throw new IllegalArgumentException("Unknown element: " + element);
        }
    }
}