package com.ecommerce.lab.utils;

import com.ecommerce.lab.model.*;
import com.ecommerce.lab.repository.base.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Component
public class TestDataGenerator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final List<String> PRODUCT_NAMES = Arrays.asList(
        "Laptop", "Smartphone", "Tablet", "Headphones", "Keyboard", "Mouse",
        "Monitor", "Printer", "Scanner", "Webcam", "Speaker", "Microphone",
        "Desk Chair", "Desk Lamp", "USB Cable", "HDMI Cable", "Power Bank",
        "Phone Case", "Screen Protector", "Smart Watch", "Fitness Tracker"
    );

    private static final List<String> BRANDS = Arrays.asList(
        "Apple", "Samsung", "Dell", "HP", "Lenovo", "Asus", "Acer",
        "Microsoft", "Sony", "LG", "Philips", "Bose", "JBL", "Logitech"
    );

    private static final List<String> CATEGORIES = Arrays.asList(
        "Electronics", "Computers", "Mobile Phones", "Accessories",
        "Audio", "Gaming", "Home Office", "Wearables"
    );

    private static final List<String> FIRST_NAMES = Arrays.asList(
        "John", "Jane", "Michael", "Sarah", "David", "Emma", "James",
        "Lisa", "Robert", "Maria", "William", "Patricia", "Richard", "Jennifer"
    );

    private static final List<String> LAST_NAMES = Arrays.asList(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
        "Miller", "Davis", "Rodriguez", "Martinez", "Wilson", "Anderson"
    );

    @Transactional
    public void generateCategories(int count) {
        if (categoryRepository.count() >= count)
            return;

        List<Category> categories = IntStream.range(0, count)
            .mapToObj(i -> {
                Category category = new Category();
                category.setName(CATEGORIES.get(i % (CATEGORIES.size()+count)) + " " + (i + 1));
                return category;
            })
            .toList();

        categoryRepository.saveAll(categories);
        System.out.println("✅ Generated " + count + " categories");
    }

    @Transactional
    public void generateProducts(int count) {
        if (categoryRepository.count() == 0) {
            generateCategories(10);
        }

        final List<Category> categories = categoryRepository.findAll();

        List<Product> products = IntStream.range(0, count)
            .parallel()
            .mapToObj(i -> {
                Product product = new Product();
                String productName = PRODUCT_NAMES.get(i % PRODUCT_NAMES.size());
                product.setName(productName + " " + (i + 1));
                product.setDescription(
                    "High-quality " + productName
                        + " with advanced features. Perfect for professional and personal use."
                );
                product.setPrice(10 + ThreadLocalRandom.current().nextDouble(990));
                product.setStock(ThreadLocalRandom.current().nextInt(0, 50));
                product.setBrand(BRANDS.get(i % BRANDS.size()));
                product.setCategory(categories.get(i % categories.size()));
                product.setImageUrl("/images/product_" + (i + 1) + ".jpg");
                return product;
            })
            .toList();

        // Save in batches for performance
        int batchSize = 500;
        for (int i = 0; i < products.size(); i += batchSize) {
            int end = Math.min(i + batchSize, products.size());
            productRepository.saveAll(products.subList(i, end));
            System.out.println("✅ Generated " + end + " products...");
        }

        System.out.println("✅ Generated " + count + " products total");
    }

    @Transactional
    public void generateUsers(int count) {
        List<User> users = IntStream.range(0, count)
            .parallel()
            .mapToObj(i -> {
                User user = new User();
                String firstName = FIRST_NAMES.get(i % FIRST_NAMES.size());
                String lastName = LAST_NAMES.get(i % LAST_NAMES.size());
                user.setName(firstName + " " + lastName);
                user.setUserName((firstName + lastName + i).toLowerCase());
                user.setEmail((firstName + "." + lastName + i + "@test.com").toLowerCase());
                user.setPassword(passwordEncoder.encode("Password123!"));
                user.setRole(Role.ROLE_USER);
                user.setStoreBalance(ThreadLocalRandom.current().nextDouble(0, 1000));
                user.setAddress(i + " Main Street, City " + i);
                user.setAge(13 + ThreadLocalRandom.current().nextInt(100));
                return user;
            })
            .toList();

        // Save in batches
        int batchSize = 200;
        for (int i = 0; i < users.size(); i += batchSize) {
            int end = Math.min(i + batchSize, users.size());
            userRepository.saveAll(users.subList(i, end));
            System.out.println("✅ Generated " + end + " users...");
        }

        System.out.println("✅ Generated " + count + " users total");
    }

    @Transactional
    public void generateReviews(int productCount, int reviewsPerProduct) {
        List<Product> products = productRepository.findAll().stream()
            .limit(productCount)
            .toList();

        List<User> users = userRepository.findAll();

        List<Review> allReviews = new ArrayList<>();

        for (Product product : products) {
            for (int i = 0; i < reviewsPerProduct && i < users.size(); i++) {
                Review review = new Review();
                review.setProduct(product);
                review.setUser(users.get(i));
                review.setRating(ThreadLocalRandom.current().nextInt(1, 6));
                review.setComment(generateRandomReviewComment());
                review.setCreatedAt(
                    LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(1, 365))
                );
                allReviews.add(review);
            }
        }

        reviewRepository.saveAll(allReviews);
        System.out.println("✅ Generated " + allReviews.size() + " reviews");
    }

    private String generateRandomReviewComment() {
        List<String> comments = Arrays.asList(
            "Excellent product! Highly recommend.",
            "Good value for money.",
            "Works as expected.",
            "Great quality, very satisfied.",
            "Decent product, but could be better.",
            "Amazing! Best purchase I've made.",
            "Not bad, but shipping was slow.",
            "Perfect condition, fast delivery.",
            "Would buy again.",
            "Fair price for the quality."
        );
        return comments.get(ThreadLocalRandom.current().nextInt(comments.size()));
    }

    @Transactional
    public void generateOrders(int count) {
        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();

        // Not implemented fully - would require Order and OrderItem entities
        System.out.println("⚠️ Order generation not implemented (requires OrderService logic)");
    }

    public void generateAllTestData(int productCount, int userCount) {
        System.out.println("🚀 Starting test data generation...");
        long startTime = System.currentTimeMillis();

        generateCategories(20);
        generateProducts(productCount);
        generateUsers(userCount);
        generateReviews(productCount, 5);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("✅ Test data generation completed in " + duration + "ms");
        System.out.println("📊 Statistics:");
        System.out.println("   - Categories: " + categoryRepository.count());
        System.out.println("   - Products: " + productRepository.count());
        System.out.println("   - Users: " + userRepository.count());
        System.out.println("   - Reviews: " + reviewRepository.count());
    }
}