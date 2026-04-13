package com.ecommerce.lab.automation.steps.setup;

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
        if (scenario.isFailed()) {
            ExtentManager.getTest().fail("Scenario Failed");
        } else {
            ExtentManager.getTest().pass("Scenario Passed");
        }
        SeleniumUtils.takeScreenshot(DriverManager.getDriver(false));
        SeleniumUtils.pause(2000);
        DriverManager.quitDriver();
    }
}
