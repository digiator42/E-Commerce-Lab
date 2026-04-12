package com.ecommerce.lab.automation.steps.setup;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.After;

public class Hooks {

    @After
    public void tearDown() {
        SeleniumUtils.takeScreenshot(DriverManager.getDriver(false));
        SeleniumUtils.pause(5000);
        DriverManager.quitDriver();
    }
}
