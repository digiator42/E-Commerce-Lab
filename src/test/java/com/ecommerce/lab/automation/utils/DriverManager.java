package com.ecommerce.lab.automation.utils;

import java.time.Duration;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.Point;

import java.awt.Toolkit;

public class DriverManager {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver(boolean sideBySide) {
        if (driver.get() == null) {
            ChromeOptions options = new ChromeOptions();
            if (System.getenv("GITHUB_ACTIONS") != null) {
                options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            }
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            WebDriver instance = new ChromeDriver(options);

            if (sideBySide) {
                sideBySide(instance);
            }
            driver.set(instance);
        }
        return driver.get();
    }

    public static WebDriver getDriverWait(boolean sideBySide) {
        WebDriver driver = getDriver(sideBySide);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    public static void sideBySide(WebDriver instance) {
        // Side by Side Logic
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int halfWidth = screenWidth / 2;

        // Determine position
        long threadId = Thread.currentThread().threadId();
        if (threadId % 2 == 0) {
            // Left Side
            instance.manage().window().setPosition(new Point(0, 0));
        } else {
            // Right Side
            instance.manage().window().setPosition(new Point(halfWidth, 0));
        }

        // Resize both to fill their respective halves
        instance.manage().window().setSize(new Dimension(halfWidth, screenHeight));
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}