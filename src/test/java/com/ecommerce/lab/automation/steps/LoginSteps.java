package com.ecommerce.lab.automation.steps;

import io.cucumber.java.en.Given;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class LoginSteps {
    private WebDriver driver;

    @Given("Site is open")
    public void site_is_open() {

        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        
        driver.get("http://localhost:8080/login");
    }
}