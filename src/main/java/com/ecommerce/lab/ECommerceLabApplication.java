package com.ecommerce.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ECommerceLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceLabApplication.class, args);
	}
}
