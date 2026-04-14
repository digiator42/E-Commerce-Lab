package com.ecommerce.lab.automation.steps.setup;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.ExtentManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        // This creates a "tab" for each scenario
        ExtentManager.createTest(scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DriverManager.getDriver(false);
        if (scenario.isFailed()) {
            ExtentManager.getTest().fail("Scenario Failed: " + scenario.getName());
            ExtentManager.addScreenshot(driver);
        } else {
            ExtentManager.getTest().pass("Scenario Passed");
            ExtentManager.addScreenshot(driver);
        }
        SeleniumUtils.takeScreenshot(DriverManager.getDriver(false));
        SeleniumUtils.pause(2000);
        DriverManager.quitDriver();
    }
}
