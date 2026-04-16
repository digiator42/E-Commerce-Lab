package com.ecommerce.lab.automation.runners;

import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Test;

@Test
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {
        "com.ecommerce.lab.automation.steps",
        "com.ecommerce.lab.automation.steps.setup"
    },
    plugin = {
        "pretty",
        "html:target/cucumber/index.html",
        "json:target/cucumber/index.json",
    },
    tags = "@E2E or @smoke",
    monochrome = true,
    publish = false
)
public class RunnerE2E extends AbstractBaseRunner {}