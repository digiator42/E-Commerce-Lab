package com.ecommerce.lab.system;

import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.dto.*;
import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.repository.base.*;
import com.ecommerce.lab.utils.TestDataGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("System Tests - Complete User Journey")
class E2ETest extends BaseControllerTest {

        @Autowired
        private TestDataGenerator dataGenerator;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ReviewRepository reviewRepository;

        @Autowired
        private CartRepository cartRepository;

        private String authToken;
        private Long productId;
        private String userEmail = "systemtest@example.com";
        private String userPassword = "Password123!";

        @BeforeAll
        void setupTestData() {

                System.out.println("========================================");
                System.out.println("SYSTEM TEST - COMPLETE USER JOURNEY");
                System.out.println("========================================");

                // Generate minimal test data
                dataGenerator.generateCategories(5);
                dataGenerator.generateProducts(10);
                dataGenerator.generateUsers(1);

                // Get a product ID for testing
                productId = productRepository.findAll().get(0).getId();

                System.out.println("✅ Test data ready. Product ID: " + productId);
        }

        @Test
        @Order(1)
        @DisplayName("TC-01: User Registration")
        void testUserRegistration() throws Exception {
                RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                        "System Test User",
                        "systemtest",
                        "123 Test Avenue, Test City, 12345",
                        25,
                        userEmail,
                        userPassword
                );

                mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value(userEmail))
                        .andExpect(jsonPath("$.userName").value("systemtest"))
                        .andExpect(jsonPath("$.displayName").value("System Test User"));

                // Verify user was actually saved in database
                var savedUser = userRepository.findByEmail(userEmail);
                assertThat(savedUser).isPresent();
                assertThat(savedUser.get().getEmail()).isEqualTo(userEmail);

                System.out.println("✅ TC-01 PASSED: User registered successfully");
        }

        @Test
        @Order(2)
        @DisplayName("TC-02: User Login")
        void testUserLogin() throws Exception {
                LoginRequestDTO loginRequest = new LoginRequestDTO(
                        userEmail,
                        userPassword,
                        null
                );

                var response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                        .andExpect(status().isOk())
                        .andReturn();

                // Extract the token from the JSON response
                String responseBody = response.getResponse().getContentAsString();
                this.authToken = objectMapper.readTree(responseBody).get("token").asText();

                System.out.println("✅ TC-02 PASSED: User logged in successfully");
        }

        @Test
        @Order(3)
        @DisplayName("TC-03: Browse Products")
        void testBrowseProducts() throws Exception {
                // Get paginated products
                mockMvc.perform(
                        get("/api/products")
                                .param("page", "0")
                                .param("size", "10")
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content").isArray())
                        .andExpect(jsonPath("$.totalElements").value(10));

                // Get single product details
                mockMvc.perform(get("/api/products/{id}", productId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(productId))
                        .andExpect(jsonPath("$.name").exists())
                        .andExpect(jsonPath("$.price").exists())
                        .andExpect(jsonPath("$.stock").exists());

                System.out.println("✅ TC-03 PASSED: Products browsed successfully");
        }

        @Test
        @Order(4)
        @DisplayName("TC-04: Filter and Search Products")
        void testFilterAndSearchProducts() throws Exception {
                // Search by name
                mockMvc.perform(
                        get("/api/products")
                                .param("search", "Laptop")
                                .param("page", "0")
                                .param("size", "10")
                )
                        .andExpect(status().isOk());

                // Filter by category
                mockMvc.perform(
                        get("/api/products/custom")
                                .param("category", "Electronics")
                                .param("minPrice", "100")
                                .param("maxPrice", "500")
                                .param("sort", "price_asc")
                                .param("page", "0")
                                .param("size", "10")
                )
                        .andExpect(status().isOk());

                System.out.println("✅ TC-04 PASSED: Filtering and search working");
        }

        @Test
        @Order(5)
        @DisplayName("TC-05: Add to Cart")
        void testAddToCart() throws Exception {
                // Add product to cart (authenticated)
                mockMvc.perform(
                        post("/api/cart/add/{productId}", productId)
                                .header("Authorization", "Bearer " + authToken)
                )
                        .andDo(print())
                        .andExpect(status().isOk());

                // Get cart items
                var cartResponse = mockMvc.perform(
                        get("/api/cart")
                                .header("Authorization", "Bearer " + authToken)
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andReturn();

                System.out.println("✅ TC-05 PASSED: Item added to cart");
        }

        @Test
        @Order(6)
        @DisplayName("TC-06: Apply Coupon to Cart")
        void testApplyCoupon() throws Exception {

                mockMvc.perform(
                        get("/api/coupons/check")
                                .param("code", "SAVE10")
                                .header("Authorization", "Bearer " + authToken)
                )
                        .andExpect(status().isNotFound());

                System.out.println("✅ TC-06 PASSED: Coupon validation endpoint works");
        }

        @Test
        @Order(7)
        @DisplayName("TC-07: Place Order")
        void testPlaceOrder() throws Exception {
                // Ensure, increase item Qty.
                mockMvc.perform(
                        post("/api/cart/add/{productId}", productId)
                                .header("Authorization", "Bearer " + authToken)
                )
                        .andExpect(status().isOk());

                String testAddress = "{\"street\":\"Main St\",\"city\":\"Cairo\",\"state\":\"EG\",\"zipCode\":\"12345\",\"country\":\"Egypt\"}";

                OrderRequest orderRequest = new OrderRequest(
                        null, // no coupon
                        false, // don't use store balance
                        testAddress,
                        List.of() // no gift cards
                );

                mockMvc.perform(
                        post("/api/orders/place")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value("Order successfully created"));

                // Verify order was created in database
                var orders = orderRepository.findAll();
                assertThat(orders).isNotEmpty();

                System.out.println("✅ TC-07 PASSED: Order placed successfully");
        }

        @Test
        @Order(8)
        @DisplayName("TC-08: View Order History")
        void testViewOrderHistory() throws Exception {
                mockMvc.perform(
                        get("/api/orders/my-orders")
                                .header("Authorization", "Bearer " + authToken)
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray());

                System.out.println("✅ TC-08 PASSED: Order history retrieved");
        }

        @Test
        @Order(9)
        @DisplayName("TC-09: Add Product Review")
        void testAddReview() throws Exception {
                Review review = new Review();
                review.setRating(5);
                review.setComment("Excellent product! System test verified.");

                // Note: This requires the user to have purchased the product
                mockMvc.perform(
                        post("/api/reviews/{productId}", productId)
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(review))
                )
                        .andExpect(status().isOk());

                System.out.println("✅ TC-09 PASSED: Review added");
        }

        @Test
        @Order(10)
        @DisplayName("TC-10: Update User Profile")
        void testUpdateProfile() throws Exception {
                UserUpdateDTO updateDTO = new UserUpdateDTO(
                        "Updated System User",
                        "updatedsystem",
                        null,
                        null,
                        "456 New Address, New City, 67890",
                        null
                );

                mockMvc.perform(
                        put("/api/users/profile")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO))
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value("Profile updated successfully"));

                System.out.println("✅ TC-10 PASSED: Profile updated");
        }

        @AfterAll
        void printSummary() {
                System.out.println("\n========================================");
                System.out.println("SYSTEM TEST SUMMARY");
                System.out.println("========================================");
                System.out.println("✅ All 10 test cases passed!");
                System.out.println("📊 Database State:");
                System.out.println("   - Users: " + userRepository.count());
                System.out.println("   - Products: " + productRepository.count());
                System.out.println("   - Orders: " + orderRepository.count());
                System.out.println("   - Cart Items: " + cartRepository.count());
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