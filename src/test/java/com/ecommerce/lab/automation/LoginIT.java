package com.ecommerce.lab.automation;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.ecommerce.lab.automation.steps",
    plugin = {
        "pretty", 
        "html:target/cucumber-reports.html",
        "json:target/cucumber.json"
    }
)
public class LoginIT extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}