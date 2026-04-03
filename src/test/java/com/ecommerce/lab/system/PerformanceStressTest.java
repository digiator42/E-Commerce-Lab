package com.ecommerce.lab.system;

import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.dto.RegisterRequestDTO;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.CartRepository;
import com.ecommerce.lab.repository.base.OrderRepository;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.ReviewRepository;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.utils.TestDataFactory;
import com.ecommerce.lab.utils.TestDataGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Performance & Stress Tests")
class PerformanceStressTest extends BaseControllerTest {

    @Autowired
    private TestDataGenerator dataGenerator;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int PRODUCT_COUNT = 10000;
    private static final int USER_COUNT = 1000;
    private static final int CONCURRENT_REQUESTS = 100;
    private static final int REQUEST_COUNT = 1000;

    private User testUser;
    private String authToken;

    Long firstProductId;

    void generateValidUSer() {
        testUser = new User();
        testUser.setEmail("testWriteCart@example.com");
        testUser.setUserName("testWriteCart");
        testUser.setName("Test Cart Add/Write");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.ROLE_USER);
        testUser.set2faEnabled(false);

        // Generate valid token
        authToken = TestDataFactory.JwtTestUtil.generateTestToken(testUser.getEmail());

        testUser.setToken(authToken);
        userRepository.save(testUser);
    }

    @BeforeAll
    void setupStressTestData() {
        System.out.println("\n========================================");
        System.out.println("PERFORMANCE & STRESS TEST SETUP");
        System.out.println("========================================");
        System.out.println("Generating " + PRODUCT_COUNT + " products...");
        System.out.println("Generating " + USER_COUNT + " users...");

        long startTime = System.currentTimeMillis();

        dataGenerator.generateCategories(20);
        dataGenerator.generateProducts(PRODUCT_COUNT);
        dataGenerator.generateUsers(USER_COUNT);
        this.generateValidUSer();

        firstProductId = productRepository.findAll().get(0).getId();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("✅ Test data generated in " + duration + "ms");
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    @DisplayName("Performance: Paginated Product Loading")
    void testPaginatedProductPerformance() throws Exception {
        System.out.println("\n📊 Testing paginated product loading performance...");

        int[] pageSizes = {
                10, 20, 50, 100
        };

        for (int pageSize : pageSizes) {
            long startTime = System.nanoTime();

            mockMvc.perform(
                get("/api/products")
                    .param("page", "0")
                    .param("size", String.valueOf(pageSize))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

            long duration = System.nanoTime() - startTime;
            double durationMs = duration / 1_000_000.0;

            System.out.printf("   Page size %d: %.2f ms%n", pageSize, durationMs);
            assertThat(durationMs).isLessThan(500); // Should load within 500ms
        }

        System.out.println("✅ Paginated loading performance test passed\n");
    }

    @Test
    @Order(2)
    @DisplayName("Performance: Product Search with Filters")
    void testProductSearchPerformance() throws Exception {
        System.out.println("\n📊 Testing product search performance...");

        String[] searchTerms = {
                "Laptop", "Phone", "Computer", "Accessories"
        };

        for (String searchTerm : searchTerms) {
            long startTime = System.nanoTime();

            mockMvc.perform(
                get("/api/products")
                    .param("search", searchTerm)
                    .param("page", "0")
                    .param("size", "20")
            )
                .andExpect(status().isOk());

            long duration = System.nanoTime() - startTime;
            double durationMs = duration / 1_000_000.0;

            System.out.printf("   Search '%s': %.2f ms%n", searchTerm, durationMs);
            assertThat(durationMs).isLessThan(1000); // Should load within 1 second
        }

        System.out.println("✅ Product search performance test passed\n");
    }

    @Test
    @Order(3)
    @DisplayName("Performance: Advanced Filtering with Multiple Criteria")
    void testAdvancedFilteringPerformance() throws Exception {
        System.out.println("\n📊 Testing advanced filtering performance...");

        long startTime = System.nanoTime();

        mockMvc.perform(
            get("/api/products/custom")
                .param("category", "Electronics")
                .param("minPrice", "100")
                .param("maxPrice", "1000")
                .param("minRating", "3")
                .param("sort", "price_asc")
                .param("page", "0")
                .param("size", "20")
        )
            .andExpect(status().isOk());

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        System.out.printf("   Advanced filter query: %.2f ms%n", durationMs);
        assertThat(durationMs).isLessThan(1500); // Should load within 1.5 seconds

        System.out.println("✅ Advanced filtering performance test passed\n");
    }

    @Test
    @Order(4)
    @DisplayName("Stress: Concurrent Product Requests")
    void testConcurrentProductRequests() throws Exception {
        System.out.println("\n📊 Testing concurrent product requests...");
        System.out.println("   Sending " + CONCURRENT_REQUESTS + " concurrent requests...");

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        List<Future<Boolean>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Instant start = Instant.now();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            futures.add(executor.submit(() -> {
                try {
                    mockMvc.perform(
                        get("/api/products")
                            .param("page", "0")
                            .param("size", "20")
                    )
                        .andExpect(status().isOk());
                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return false;
                }
            }));
        }

        // Wait for all requests to complete
        for (Future<Boolean> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        double requestsPerSecond = (REQUEST_COUNT * 1000.0) / duration;

        executor.shutdown();

        System.out.printf("   Total requests: %d%n", REQUEST_COUNT);
        System.out.printf("   Successful: %d%n", successCount.get());
        System.out.printf("   Failed: %d%n", failureCount.get());
        System.out.printf("   Total time: %d ms%n", duration);
        System.out.printf("   Requests/second: %.2f%n", requestsPerSecond);

        assertThat(successCount.get()).isEqualTo(REQUEST_COUNT);
        assertThat(requestsPerSecond).isGreaterThan(50); // Should handle at least 50 req/sec

        System.out.println("✅ Concurrent requests stress test passed\n");
    }

    @Test
    @Order(5)
    @DisplayName("Stress: Concurrent User Registrations")
    void testConcurrentUserRegistrations() throws Exception {
        System.out.println("\n📊 Testing concurrent user registrations...");

        int concurrentUsers = 50;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Boolean>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Instant start = Instant.now();

        for (int i = 0; i < concurrentUsers; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                try {
                    RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                        "Stress User " + index,
                        "stressuser" + index,
                        "Stress Address " + index,
                        25,
                        "stress" + index + "@test.com",
                        "Password123!"
                    );

                    mockMvc.perform(
                        post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest))
                    )
                        .andExpect(status().isOk());

                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return false;
                }
            }));
        }

        // Wait for all registrations
        for (Future<Boolean> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();

        executor.shutdown();

        System.out.printf("   Concurrent registrations: %d%n", concurrentUsers);
        System.out.printf("   Successful: %d%n", successCount.get());
        System.out.printf("   Failed: %d%n", failureCount.get());
        System.out.printf("   Total time: %d ms%n", duration);

        assertThat(successCount.get()).isEqualTo(concurrentUsers);

        System.out.println("✅ Concurrent registrations stress test passed\n");
    }

    @Test
    @Order(6)
    @DisplayName("Performance: Database Query Optimization")
    void testDatabaseQueryPerformance() throws Exception {
        System.out.println("\n📊 Testing database query performance...");

        // Test product details with reviews (uses @Formula)
        List<Long> productIds = productRepository.findAll().stream()
            .limit(10)
            .map(product -> product.getId())
            .toList();

        for (Long productId : productIds) {
            long startTime = System.nanoTime();

            mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").exists())
                .andExpect(jsonPath("$.totalReviews").exists());

            long duration = System.nanoTime() - startTime;
            double durationMs = duration / 1_000_000.0;

            System.out.printf("   Product %d: %.2f ms%n", productId, durationMs);
            assertThat(durationMs).isLessThan(50); // Individual product loads within 50ms
        }

        System.out.println("✅ Database query performance test passed\n");
    }

    @Test
    @Order(7)
    @DisplayName("Stress: Mixed Workload (Reads + Writes)")
    void testMixedWorkload() throws Exception {
        System.out.println("\n📊 Testing mixed workload (concurrent reads and writes)...");

        int operations = 200;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<Boolean>> futures = new ArrayList<>();
        AtomicInteger readSuccess = new AtomicInteger(0);
        AtomicInteger writeSuccess = new AtomicInteger(0);

        // get products with stock more than 20
        List<Product> products = productRepository.findByStockGreaterThan(20);

        Instant start = Instant.now();

        for (int i = 0; i < operations; i++) {
            final boolean isRead = i % 2 == 0;
            final Long pId = products.get(i).getId();

            futures.add(executor.submit(() -> {
                try {
                    if (isRead) {
                        // Read operation
                        mockMvc.perform(
                            get("/api/products")
                                .param("page", "0")
                                .param("size", "20")
                        )
                            .andExpect(status().isOk());
                        readSuccess.incrementAndGet();
                    } else {
                        mockMvc.perform(
                            post("/api/cart/add/{productId}", pId)
                                .header("Authorization", "Bearer " + authToken)
                        )
                            .andExpect(status().isOk());
                        writeSuccess.incrementAndGet();
                    }
                    return true;
                } catch (Exception e) {
                    System.err
                        .println("Failed for productId=" + pId + " with error: " + e.getMessage());
                    return false;
                }
            }));
        }

        for (Future<Boolean> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();

        executor.shutdown();

        System.out.printf("   Read operations: %d success%n", readSuccess.get());
        System.out.printf("   Write operations: %d success%n", writeSuccess.get());
        System.out.printf("   Total time: %d ms%n", duration);
        System.out.printf("   Throughput: %.2f ops/sec%n", (operations * 1000.0) / duration);

        assertThat(readSuccess.get() + writeSuccess.get()).isEqualTo(operations);

        System.out.println("✅ Mixed workload stress test passed\n");
    }

    @AfterAll
    void printPerformanceSummary() {
        System.out.println("\n========================================");
        System.out.println("PERFORMANCE TEST SUMMARY");
        System.out.println("========================================");
        System.out.println("📊 Test Environment:");
        System.out.println("   - Products: " + productRepository.count());
        System.out.println("   - Users: " + userRepository.count());
        System.out.println("   - Concurrent requests: " + CONCURRENT_REQUESTS);
        System.out.println("   - Total requests: " + REQUEST_COUNT);
        System.out.println("========================================");
        System.out.println("✅ All performance and stress tests passed!");
        System.out.println("========================================\n");

        this.tearDown();
    }

    void tearDown() {
        // Delete children
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        reviewRepository.deleteAll();
        // Delete parents
        productRepository.deleteAll();
        userRepository.deleteAll();

        System.out.println("✅ Database cleaned successfully");
    }
}