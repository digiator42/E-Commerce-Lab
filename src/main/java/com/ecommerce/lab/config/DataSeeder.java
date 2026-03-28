package com.ecommerce.lab.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.lab.model.Category;
import com.ecommerce.lab.model.Coupon;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.CategoryRepository;
import com.ecommerce.lab.repository.base.CouponRepository;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.ReviewRepository;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.EmailService;

@Configuration
@Profile("!test") // Don't run data seeder during tests
public class DataSeeder {

    private EmailService emailService;

    String[] reviewComments = {
            "Excellent quality!", "Highly recommend.", "Good value for money.",
            "Satisfied with the purchase.", "Exactly as described."
    };

    public DataSeeder(EmailService emailService) { this.emailService = emailService; }

    @Bean
    CommandLineRunner initDatabase(
        ProductRepository productRepo,
        CategoryRepository categoryRepo,
        UserRepository userRepo,
        PasswordEncoder passwordEncoder,
        CouponRepository couponRepository,
        ReviewRepository reviewRepository
    ) {

        return args -> {

            User admin = createAdminUser(userRepo, passwordEncoder);
            List<User> users = createMockUsers(userRepo, passwordEncoder);

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

                int index = 0;
                for (String productName : itemsInormation.keySet()) {
                    Product p = new Product();
                    p.setName(productName);
                    p.setCategory(currentCategory);

                    // Set Price and Stock
                    double price = generatePriceByCategory(categoryName, index);
                    p.setPrice(Math.round(price * 100.0) / 100.0);
                    p.setStock(generateStockByCategory(categoryName, index));

                    List<Review> reviews = new ArrayList<>();
                    p = productRepo.save(p);

                    Random random = new Random();
                    int maxIteration = random.nextInt(5) + 1;

                    for (int i = 0; i < maxIteration; i++) {
                        int maxRate = random.nextInt(5) + 1;
                        Review review = new Review();
                        review.setRating(maxRate);
                        review.setComment(reviewComments[i % reviewComments.length]);
                        review.setProduct(p);
                        review.setUser(users.get(maxRate - 1));
                        // reviewRepository.save(review);
                        reviews.add(review);
                    }
                    p.setReviews(reviews);

                    // Set Image (Directly from the map we are looping through)
                    String imageUrl = itemsInormation.get(productName);
                    p.setImageUrl(
                        imageUrl != null ? imageUrl
                            : CategoryImages.getFallbackImageUrl(categoryName)
                    );

                    // Set Description (Cycle through descriptions if you have fewer descriptions
                    // than products)
                    if (descriptionsForCat != null && !descriptionsForCat.isEmpty()) {
                        String desc = descriptionsForCat.get(index % descriptionsForCat.size());
                        p.setDescription(
                            desc + ". Perfect for everyday use. "
                                + (index % 2 == 0 ? "Limited edition." : "Best seller.")
                        );
                    }

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

    private User createAdminUser(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        // this.emailService.sendSimpleEmail(
        // "aimlive2013@gmail.com",
        // "Testing",
        // "This is a test email\n"
        // );

        if (userRepo.count() > 0) {
            return null;
        }

        User admin = new User();
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setUserName("Admino");
        admin.setRole(Role.ROLE_ADMIN);
        userRepo.save(admin);
        System.out.println("==> Created admin user successfully.");

        return admin;
    }

    private List<User> createMockUsers(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        String[] names = {
                "Ahmed Zaki", "Sarah Connor", "John Doe", "Layla Hassan", "Mike Ross"
        };
        String[] emails = {
                "ahmed@example.com", "sarah@example.com", "john@example.com", "layla@example.com",
                "mike@example.com"
        };

        List<User> mockUsers = new ArrayList<>();

        for (int i = 0; i < names.length; i++) {
            // Only create if the user doesn't already exist
            if (userRepo.findByEmail(emails[i]).isEmpty()) {
                User user = new User();
                user.setName(names[i]);
                user.setUserName(names[i].split(" ")[0] + "o");
                user.setEmail(emails[i]);
                user.setPassword(passwordEncoder.encode("password123"));

                user.setRole(Role.ROLE_USER);

                user.setAddress(
                    "{\"street\":\"Main St " + i
                        + "\",\"city\":\"Cairo\",\"state\":\"EG\",\"zipCode\":\"12345\",\"country\":\"Egypt\"}"
                );

                mockUsers.add(user);
            }
        }

        userRepo.saveAll(mockUsers);
        System.out.println("====> Inserted " + mockUsers.size() + " mock users.");

        return mockUsers;
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
