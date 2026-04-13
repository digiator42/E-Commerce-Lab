package com.ecommerce.lab.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;


import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import com.ecommerce.lab.automation.utils.ExtentManager;


public abstract class AbstractBaseRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void globalSetup() {
        System.out.println("--- QC AUTOMATION: Global Suite Setup Starting ---");
        ExtentManager.getInstance();

    }

    @AfterSuite
    public void globalTeardown() {
        System.out.println("--- QC AUTOMATION: Global Suite Teardown ---");
        ExtentManager.getInstance().flush();
    }

    @Override
    @DataProvider(parallel = true) 
    public Object[][] scenarios() {
        return super.scenarios();
    }
}