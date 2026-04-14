package com.ecommerce.lab.automation.steps;

import com.ecommerce.lab.automation.components.Product;
import com.ecommerce.lab.automation.pages.ProductPage;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.And;

public class CartSteps {

    ProductPage productPage = new ProductPage(DriverManager.getDriver(true));

    @And("user adds product {string} to the cart")
    public void one_item_is_added_to_the_cart(String productId) {

        Product basketball = productPage
            .getProduct(
                p -> p.getProductId().toString().equals(productId)
            );
            
        basketball.addToCart();
    }
}
