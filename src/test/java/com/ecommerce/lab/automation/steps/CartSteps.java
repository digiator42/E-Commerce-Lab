package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.components.Navbar;
import com.ecommerce.lab.automation.components.Product;
import com.ecommerce.lab.automation.pages.ProductPage;
import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CartSteps {

    // final WebDriver driver = DriverManager.getSmartDriver();

    private WebDriver getDriver() { return DriverManager.getSmartDriver(); }

    @And("user adds product {string} to the cart")
    public void one_item_is_added_to_the_cart(String productId) {
        ProductPage productPage = new ProductPage(getDriver());
        Navbar navbar = new Navbar(getDriver());
        navbar.goToStore();

        SeleniumUtils.pause(2000);

        Product basketball = productPage
            .getProduct(
                p -> p.getProductId().toString().equals(productId)
            );

        basketball.addToCart();
    }

    @When("the user adds {string} to the shopping cart")
    public void the_user_adds_product_name_to_the_shopping_cart(String productName) {
        WebDriver driver = getDriver();
        Navbar navbar = new Navbar(driver);
        navbar.goToStore();

        SeleniumUtils.pause(2000);

        ProductPage productPage = new ProductPage(driver);

        Product product = productPage
            .getProduct(
                p -> p.getName().trim().equalsIgnoreCase(productName)
            );

        product.addToCart();

    }

    @Then("the cart badge should display one item")
    public void the_cart_badge_should_display_item() {
        // verifying cart badge display
        WebDriver driver = getDriver();
        Navbar navbar = new Navbar(driver);
        int badgeText = navbar.getCartCount();
        assertThat(badgeText, is(0));
    }
}
