package com.ecommerce.lab.config;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.ProductRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            System.out.println("🛠️ Checking database for seeding...");
            List<Product> products = IntStream.rangeClosed(1, 50)
                    .mapToObj(i -> {
                        Product p = new Product();
                        p.setName("Product " + i);
                        p.setDescription("Item number " + i + " in the store.");
                        p.setPrice(10.0 * i);
                        p.setStock(i + 5);
                        return p;
                    }).toList();

            repository.saveAll(products);
            System.out.println("50 Products created.");
        };
    }
}