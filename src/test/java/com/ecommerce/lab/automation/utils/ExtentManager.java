package com.ecommerce.lab.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/QC-Report.html");
            spark.config().setReportName("Ecommerce Lab Test Suite");
            spark.config().setDocumentTitle("QC Automation Results");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", "GitHub Actions / Local");
            extent.setSystemInfo("QA", "Automation Specialist");
        }
        return extent;
    }

    public static ExtentTest getTest() { return test.get(); }

    public static void createTest(String name) { test.set(extent.createTest(name)); }

    public static void addScreenshot(WebDriver driver) {
        if (driver instanceof TakesScreenshot) {
            // Capture screenshot as Base64 string
            String base64Code = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

            // Attach it to the current thread's test
            getTest().info(
                "Screen Capture",
                MediaEntityBuilder.createScreenCaptureFromBase64String(base64Code).build()
            );
        }
    }
}