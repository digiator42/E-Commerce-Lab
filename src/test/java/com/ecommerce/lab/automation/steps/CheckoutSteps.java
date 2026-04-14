package com.ecommerce.lab.automation.steps;

import com.ecommerce.lab.automation.pages.CheckoutPage;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.When;

public class CheckoutSteps {

    private CheckoutPage checkoutPage = new CheckoutPage(DriverManager.getParallelDriverWait(true));

    @When("the user proceeds to checkout")
    public void the_user_proceeds_to_checkout() {
        checkoutPage.proceedToCheckout();
    }

}
