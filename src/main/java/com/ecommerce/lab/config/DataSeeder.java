package com.ecommerce.lab.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ecommerce.lab.service.EmailService;

@Configuration
public class DataSeeder {

    private EmailService emailService;

    public DataSeeder(EmailService emailService) { this.emailService = emailService; }

    @Bean
    CommandLineRunner initDatabase(
        ProductRepository productRepo,
        CategoryRepository categoryRepo,
        UserRepository userRepo,
        PasswordEncoder passwordEncoder,
        CouponRepository couponRepository
    ) {

        return args -> {

            createAdminUser(userRepo, passwordEncoder);
            createValidCoupons(couponRepository);

            if (productRepo.count() > 0) {
                return;
            }

            // 1. Create and Save Categories with more realistic names
            List<Category> categories = List.of(
                createCat("Electronics", "🔌"),
                createCat("Clothing", "👕"),
                createCat("Home & Garden", "🏡"),
                createCat("Books", "📚"),
                createCat("Sports", "⚽"),
                createCat("Ramadan", "🕌")
            );
            categoryRepo.saveAll(categories);

            // Generate 50 products
            List<Product> products = new ArrayList<>();

            // Initialize all images
            CategoryImages.setBooksImages();
            CategoryImages.setClothingImages();
            CategoryImages.setElectronicsImages();
            CategoryImages.setHomeGardenImages();
            CategoryImages.setSportsImages();
            CategoryImages.setRamadanImages();

            // Iterate through each category
            for (String categoryName : CategoryImages.categoryImageUrls.keySet()) {

                // Find the actual Category object from your 'categories' list
                Category currentCategory = categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst()
                    .orElse(null);

                if (currentCategory == null) {
                    continue;
                }

                // Loop through every product for this category
                Map<String, String> itemsInormation = CategoryImages.categoryImageUrls
                    .get(categoryName);
                List<String> descriptionsForCat = ProductDetails.descriptions.get(categoryName);

                for (int i = 0; i < 5; i++) {
                    // reviews.add(new Review)
                }

                int index = 0;
                for (String productName : itemsInormation.keySet()) {
                    Product p = new Product();
                    p.setName(productName);
                    p.setCategory(currentCategory);

                    // Set Image (Directly from the map we are looping through)
                    String imageUrl = itemsInormation.get(productName);
                    p.setImageUrl(
                        imageUrl != null ? imageUrl
                            : CategoryImages.getFallbackImageUrl(categoryName)
                    );

                    // p.s
                    // Set Description (Cycle through descriptions if you have fewer descriptions
                    // than products)
                    if (descriptionsForCat != null && !descriptionsForCat.isEmpty()) {
                        String desc = descriptionsForCat.get(index % descriptionsForCat.size());
                        p.setDescription(
                            desc + ". Perfect for everyday use. "
                                + (index % 2 == 0 ? "Limited edition." : "Best seller.")
                        );
                    }

                    // Set Price and Stock
                    double price = generatePriceByCategory(categoryName, index);
                    p.setPrice(Math.round(price * 100.0) / 100.0);
                    p.setStock(generateStockByCategory(categoryName, index));

                    products.add(p);
                    index++;
                }
            }

            productRepo.saveAll(products);
            System.out.println(
                "✅ Seeded "
                    + products.size()
                    + " products with realistic data and images successfully."
            );

        };
    }

    private Category createCat(String name, String icon) {
        Category cat = new Category();
        cat.setName(name);
        cat.setIcon(icon);
        return cat;
    }

    private double generatePriceByCategory(String category, int index) {
        return switch (category) {
        case "Electronics" -> 29.99 + (index * 15.5) % 500; // $30 - $530
        case "Clothing" -> 19.99 + (index * 7.3) % 150; // $20 - $170
        case "Home & Garden" -> 24.99 + (index * 12.1) % 300; // $25 - $325
        case "Books" -> 12.99 + (index * 3.7) % 40; // $13 - $53
        case "Sports" -> 15.99 + (index * 9.2) % 200; // $16 - $216
        default -> 29.99;
        };
    }

    private int generateStockByCategory(String category, int index) {
        int outOfStock[] = new int[] {
                1, 2, 3, 4
        };
        boolean isOutOfStock = Arrays.stream(outOfStock).anyMatch(i -> i == index);

        return switch (category) {
        case "Electronics" -> isOutOfStock ? 0 : 5 + (index % 20); // 5-25 items
        case "Clothing" -> isOutOfStock ? 0 : 15 + (index % 50); // 15-65 items
        case "Home & Garden" -> isOutOfStock ? 0 : 8 + (index % 30); // 8-38 items
        case "Books" -> isOutOfStock ? 0 : 10 + (index % 40); // 10-50 items
        case "Sports" -> isOutOfStock ? 0 : 12 + (index % 35); // 12-47 items
        case "Ramadan" -> isOutOfStock ? 0 : 12 + (index % 35); // 12-47 items
        default -> 0;
        };
    }

    private void createAdminUser(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.emailService.sendSimpleEmail(
            "aimlive2013@gmail.com",
            "Testing",
            "This is a test email\n"
        );

        if (userRepo.count() > 0) {
            return;
        }

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
