package com.ecommerce.lab.automation.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class SeleniumUtils {

    public static final String BASE_URL = "http://localhost:8080";

    /**
     * Waits for an element to be clickable and then clicks it
     */
    public static void waitAndClick(
        WebDriver driver,
        By locator,
        int timeoutInSeconds
    ) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.elementToBeClickable(locator))
            .click();
    }

    /**
     * Agressive wait and click using JavaScript to bypass potential WebDriver
     * issues in CI environments
     */
    public static void hardWaitAndClick(
        WebDriver driver,
        By locator,
        int timeoutInSeconds
    ) {
        try {
            // Try a standard click first
            waitAndClick(driver, locator, timeoutInSeconds);
        } catch (ElementClickInterceptedException e) {
            // Try with js
            waitAndClickJS(driver, locator, timeoutInSeconds);
        }
    }

    /**
     * Waits for an element to be visible
     */
    public static WebElement waitForElement(
        WebDriver driver,
        By locator,
        int timeoutInSeconds
    ) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for an element to be invisible
     */
    public static void waitForElementToDisappear(
        WebDriver driver,
        By locator,
        int timeoutInSeconds
    ) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Waits for an element to be present in the DOM
     */
    public static WebElement waitForElementPresence(
        WebDriver driver,
        By locator,
        int timeoutInSeconds
    ) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits for an element to be visible then clicks it using JavaScript
     */
    public static void waitAndClickJS(
        WebDriver driver,
        By locator,
        int timeoutInSeconds
    ) {
        WebElement element = waitForElement(driver, locator, timeoutInSeconds);
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }

    /**
     * Scroll into View of an element using JavaScript
     */
    public static void scrollToElement(WebDriver driver, By locator, int timeoutInSeconds) {
        WebElement elementToScroll = waitForElement(driver, locator, timeoutInSeconds);
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView();", elementToScroll);
    }

    /**
     * Navigation helper using the Base URL
     */
    public static void navigateTo(
        WebDriver driver,
        String path
    ) { driver.get(BASE_URL + path); }

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
        if (driver instanceof TakesScreenshot) {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                FileUtils.copyFile(
                    scrFile, new File(filePath + "_" + System.currentTimeMillis() + ".png")
                );
            } catch (IOException e) {
                System.err.println("Failed to save screenshot: " + e.getMessage());
            }
        }

        System.out.println("Taking screenshot and saving to: " + filePath);
    }
}
