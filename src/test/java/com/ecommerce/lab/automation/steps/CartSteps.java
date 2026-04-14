package com.ecommerce.lab.automation.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.components.Navbar;
import com.ecommerce.lab.automation.components.Product;
import com.ecommerce.lab.automation.pages.ProductPage;
import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.And;

public class CartSteps {

    final WebDriver driver = DriverManager.getParallelDriverWait(true);
    ProductPage productPage = new ProductPage(driver);

    @And("user adds product {string} to the cart")
    public void one_item_is_added_to_the_cart(String productId) {
        Navbar navbar = new Navbar(DriverManager.getParallelDriverWait(false));

        navbar.goToStore();

        Product basketball = productPage
            .getProduct(
                p -> p.getProductId().toString().equals(productId)
            );

        basketball.addToCart();
    }
}
