package com.ecommerce.lab.config;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.lab.model.Category;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.CategoryRepository;
import com.ecommerce.lab.repository.ProductRepository;
import com.ecommerce.lab.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            ProductRepository productRepo,
            CategoryRepository categoryRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            createAdminUser(userRepo, passwordEncoder);

            if (productRepo.count() > 0)
                return;

            // 1. Create and Save Categories
            List<Category> categories = List.of(
                    createCat("Electronics", "🔌"),
                    createCat("Clothing", "👕"),
                    createCat("Home & Garden", "🏡"),
                    createCat("Books", "📚"),
                    createCat("Sports", "⚽"));
            categoryRepo.saveAll(categories);

            // 2. Generate Products
            List<Product> products = IntStream.rangeClosed(1, 50)
                    .mapToObj(i -> {
                        Product p = new Product();
                        p.setName("Product " + i);
                        p.setDescription("Description for " + i);
                        p.setPrice(Math.round((10.0 + Math.random() * 90) * 100.0) / 100.0);
                        p.setStock(10);

                        // Assign a category from our saved list using modulo
                        p.setCategory(categories.get(i % categories.size()));

                        return p;
                    }).toList();

            productRepo.saveAll(products);
            System.out.println("✅ Seeded categories and products successfully.");

        };
    }

    private void createAdminUser(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        if (userRepo.count() > 0)
            return;

        User admin = new User();
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setUserName("Admino");
        admin.setRole(Role.ROLE_ADMIN);
        userRepo.save(admin);
        System.out.println("==> Created admin user successfully.");
    }

    private Category createCat(String name, String icon) {
        Category c = new Category();
        c.setName(name);
        c.setIcon(icon);
        return c;
    }
}