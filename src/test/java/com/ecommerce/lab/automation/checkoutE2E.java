package com.ecommerce.lab.automation;

import io.cucumber.testng.CucumberOptions;

import com.ecommerce.lab.automation.runners.AbstractBaseRunner;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.ecommerce.lab.automation.steps",
    plugin = {
        "pretty", 
        "html:target/cucumber/report.html",
        "json:target/cucumber/cucumber.json"
    }
)
public class checkoutE2E extends AbstractBaseRunner {
    

}