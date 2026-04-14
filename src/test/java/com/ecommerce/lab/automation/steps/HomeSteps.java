package com.ecommerce.lab.automation.steps;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.Given;

public class HomeSteps {

    @Given("the user is on the home page")
    public void the_user_is_on_the_e_commerce_lab_home_page() {
        WebDriver driver = DriverManager.getSmartDriver();
        SeleniumUtils.navigateTo(driver, "/");
    }

}
