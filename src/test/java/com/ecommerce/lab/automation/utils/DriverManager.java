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
    // ThreadLocal for Parallel Execution
    private static ThreadLocal<WebDriver> threadDriver = new ThreadLocal<>();

    // Normal driver
    private static WebDriver normalDriver;

    /**
     * Standard Driver.
     */
    public static WebDriver getDriver() {
        if (normalDriver == null) {
            normalDriver = createInstance(false);
        }
        return normalDriver;
    }

    /**
     * Parallel Driver (Thread-Safe) Actions.
     */
    public static WebDriver getParallelDriver(boolean sideBySide) {
        if (threadDriver.get() == null) {
            WebDriver instance = createInstance(sideBySide);
            threadDriver.set(instance);
        }
        return threadDriver.get();
    }

    /**
     * Parallel Driver with Implicit Wait
     */
    public static WebDriver getParallelDriverWait(boolean sideBySide) {
        WebDriver driver = getParallelDriver(sideBySide);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    private static WebDriver createInstance(boolean sideBySide) {
        ChromeOptions options = new ChromeOptions();
        boolean isGitHubActions = System.getenv("GITHUB_ACTIONS") != null;

        if (isGitHubActions) {
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        }

        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        WebDriver instance = new ChromeDriver(options);

        if (sideBySide && !isGitHubActions) {
            applySideBySide(instance);
        }
        return instance;
    }

    private static void applySideBySide(WebDriver instance) {
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int halfWidth = (int) screenSize.getWidth() / 2;
        int screenHeight = (int) screenSize.getHeight();

        long threadId = Thread.currentThread().threadId();
        int xPosition = (threadId % 2 == 0) ? 0 : halfWidth;

        instance.manage().window().setPosition(new Point(xPosition, 0));
        instance.manage().window().setSize(new Dimension(halfWidth, screenHeight));
    }

    public static void quitDriver() {
        // Quit Parallel Driver
        if (threadDriver.get() != null) {
            threadDriver.get().quit();
            threadDriver.remove();
        }
        // Quit Normal Driver
        if (normalDriver != null) {
            normalDriver.quit();
            normalDriver = null;
        }
    }
}