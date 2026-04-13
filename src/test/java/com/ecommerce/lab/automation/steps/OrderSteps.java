package com.ecommerce.lab.automation.steps;

import com.ecommerce.lab.automation.pages.CheckoutPage;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.Then;

public class OrderSteps {
    CheckoutPage checkoutPage = new CheckoutPage(DriverManager.getDriver(true));

    @Then("the order should be successful for {string}")
    public void the_order_should_be_successful_for(String string) {
        checkoutPage.isOrderSuccessful();
    }

}
