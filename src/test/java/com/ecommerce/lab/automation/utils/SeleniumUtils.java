package com.ecommerce.lab.automation.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class SeleniumUtils {

    public static final String BASE_URL = "http://localhost:8080";

    /**
     * Waits for an element to be clickable and then clicks it
     */
    public static void waitAndClick(WebDriver driver, By locator, int timeoutInSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.elementToBeClickable(locator))
            .click();
    }

    /**
     * Waits for an element to be visible
     */
    public static WebElement waitForElement(WebDriver driver, By locator, int timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Navigation helper using the Base URL
     */
    public static void navigateTo(WebDriver driver, String path) { driver.get(BASE_URL + path); }

    /**
     * Thread sleep wrapper to keep step definitions clean of try-catch blocks
     */
    public static void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread sleep interrupted: " + e.getMessage());
        }
    }

    /**
     * Takes a screenshot and saves it to the specified path
     */
    public static void takeScreenshot(WebDriver driver) {
        String filePath = "screenshots/screenshot";
        if (driver instanceof org.openqa.selenium.TakesScreenshot) {
            java.io.File scrFile = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            try {
                org.apache.commons.io.FileUtils.copyFile(scrFile, new java.io.File(filePath + "_" + System.currentTimeMillis() + ".png"));
            } catch (java.io.IOException e) {
                System.err.println("Failed to save screenshot: " + e.getMessage());
            }
        }
        
        System.out.println("Taking screenshot and saving to: " + filePath);
    }
}
