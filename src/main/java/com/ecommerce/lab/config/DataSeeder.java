package com.ecommerce.lab.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.lab.model.Category;
import com.ecommerce.lab.model.Coupon;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.CategoryRepository;
import com.ecommerce.lab.repository.CouponRepository;
import com.ecommerce.lab.repository.ProductRepository;
import com.ecommerce.lab.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            ProductRepository productRepo,
            CategoryRepository categoryRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder,
            CouponRepository couponRepository) {

        return args -> {

            createAdminUser(userRepo, passwordEncoder);
            createValidCoupons(couponRepository);

            if (productRepo.count() > 0)
                return;

            // 1. Create and Save Categories with more realistic names
            List<Category> categories = List.of(
                    createCat("Electronics", "🔌"),
                    createCat("Clothing", "👕"),
                    createCat("Home & Garden", "🏡"),
                    createCat("Books", "📚"),
                    createCat("Sports", "⚽"));
            categoryRepo.saveAll(categories);

            // Generate 50 products
            List<Product> products = new ArrayList<>();

            for (int i = 1; i <= 200; i++) {
                Product p = new Product();

                // Get category based on index
                Category category = categories.get(i % categories.size());
                String categoryName = category.getName();

                // Get product name from the category's list
                List<String> namesForCategory = ProductDetails.productNames.get(categoryName);
                String baseProductName = namesForCategory.get(i % namesForCategory.size());

                // Add version number for products beyond the base list
                String productName = baseProductName;
                if (i > namesForCategory.size()) {
                    int version = (i / namesForCategory.size()) + 1;
                    productName = baseProductName + " v" + version;
                }
                p.setName(productName);

                // Set realistic description
                List<String> descForCategory = ProductDetails.descriptions.get(categoryName);
                String description = descForCategory.get(i % descForCategory.size());
                description += ". Perfect for everyday use. " + (i % 2 == 0 ? "Limited edition." : "Best seller.");
                p.setDescription(description);

                // Set realistic price based on category
                double price = generatePriceByCategory(categoryName, i);
                p.setPrice(Math.round(price * 100.0) / 100.0);

                // Set realistic stock
                p.setStock(generateStockByCategory(categoryName, i));

                // Set image URL from our mappings
                Map<String, String> categoryImages = CategoryImages.categoryImageUrls.get(categoryName);
                if (categoryImages != null && categoryImages.containsKey(baseProductName)) {
                    p.setImageUrl(categoryImages.get(baseProductName));
                } else {
                    // Fallback image if not found
                    p.setImageUrl(CategoryImages.getFallbackImageUrl(categoryName));
                }

                // Assign category
                p.setCategory(category);

                products.add(p);
            }

            productRepo.saveAll(products);
            System.out.println("✅ Seeded " +
                    products.size() +
                    " products with realistic data and images successfully.");

        };
    }

    private Category createCat(String name, String icon) {
        Category cat = new Category();
        cat.setName(name);
        cat.setIcon(icon);
        return cat;
    }

    private double generatePriceByCategory(String category, int index) {
        switch (category) {
            case "Electronics":
                return 29.99 + (index * 15.5) % 500; // $30 - $530
            case "Clothing":
                return 19.99 + (index * 7.3) % 150; // $20 - $170
            case "Home & Garden":
                return 24.99 + (index * 12.1) % 300; // $25 - $325
            case "Books":
                return 12.99 + (index * 3.7) % 40; // $13 - $53
            case "Sports":
                return 15.99 + (index * 9.2) % 200; // $16 - $216
            default:
                return 29.99;
        }
    }

    private int generateStockByCategory(String category, int index) {
        switch (category) {
            case "Electronics":
                return 5 + (index % 20); // 5-25 items
            case "Clothing":
                return 15 + (index % 50); // 15-65 items (more sizes available)
            case "Home & Garden":
                return 8 + (index % 30); // 8-38 items
            case "Books":
                return 10 + (index % 40); // 10-50 items
            case "Sports":
                return 12 + (index % 35); // 12-47 items
            default:
                return 10;
        }
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

    private void createValidCoupons(CouponRepository couponRepository) {
        if (couponRepository.count() > 0) {
            return;
        }

        Coupon coupon = new Coupon();

        coupon.setCode("RAMADAN20");
        coupon.setDiscountPercentage(20);
        coupon.setUsageLimit(10);
        coupon.setExpiryDate(LocalDate.now().plusDays(20));
        coupon.setActive(true);
        couponRepository.save(coupon);

        Coupon coupon2 = new Coupon();

        coupon2.setCode("EID2026");
        coupon2.setDiscountPercentage(30);
        coupon2.setUsageLimit(10);
        coupon2.setExpiryDate(LocalDate.now().plusDays(24));
        coupon2.setActive(true);
        couponRepository.save(coupon2);

    }
}