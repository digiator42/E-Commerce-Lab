package com.ecommerce.lab.dev.system;

import com.ecommerce.lab.dev.BaseControllerTest;
import com.ecommerce.lab.dev.utils.TestDataGenerator;
import com.ecommerce.lab.dto.*;
import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.repository.base.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class E2EAPIFlowTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestDataGenerator testDataGenerator;

    private static String testUserEmail = "e2e@test.com";
    private static String testUserPassword = "E2ETest123!";
    private static Long testProductId;
    private static Long testOrderId;
    private static String validToken;

    @BeforeAll
    void init() { testDataGenerator.generateProducts(10); }

    @Test
    @Order(1)
    @DisplayName("E2E-01: Complete API Flow - Register to Order")
    void testCompleteAPIFlow() throws Exception {
        System.out.println("\n========================================");
        System.out.println("E2E TEST - COMPLETE API FLOW");
        System.out.println("========================================\n");

        String testAddress = "{\"street\":\"Main St\",\"city\":\"Cairo\",\"state\":\"EG\",\"zipCode\":\"12345\",\"country\":\"Egypt\"}";

        // Step 1: Register
        System.out.println("Step 1: Registering user...");
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
            "E2E Test User",
            "e2euser",
            testAddress,
            30,
            testUserEmail,
            testUserPassword
        );

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(testUserEmail));

        System.out.println("   ✓ User registered");

        // Step 2: Login
        System.out.println("Step 2: Logging in...");
        LoginRequestDTO loginRequest = new LoginRequestDTO(
            testUserEmail,
            testUserPassword,
            null
        );

        var response = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk())
            .andReturn();

        System.out.println("   ✓ User logged in");

        var responseUser = response.getResponse().getContentAsString();
        validToken = objectMapper.readTree(responseUser).get("token").asText();

        // Step 3: Get products
        System.out.println("Step 3: Fetching products...");
        var productsResponse = mockMvc.perform(
            get("/api/products")
                .param("page", "0")
                .param("size", "5")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andReturn();

        // Extract first product ID
        String responseContent = productsResponse.getResponse().getContentAsString();
        // Parse JSON to get product ID (simplified - in real test use JSON parsing)
        testProductId = productRepository.findAll().get(0).getId();
        System.out.println("   ✓ Products fetched, using product ID: " + testProductId);

        // Step 4: Add to cart
        System.out.println("Step 4: Adding product to cart...");
        mockMvc.perform(
            post("/api/cart/add/{productId}", testProductId)
                .header("Authorization", "Bearer " + validToken)
        )
            .andDo(print())
            .andExpect(status().isOk());
        System.out.println("   ✓ Product added to cart");

        // Step 5: View cart
        System.out.println("Step 5: Viewing cart...");
        mockMvc.perform(
            get("/api/cart")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
        System.out.println("   ✓ Cart retrieved");

        // Step 6: Place order
        System.out.println("Step 6: Placing order...");
        OrderRequest orderRequest = new OrderRequest(
            null,
            false,
            testAddress,
            List.of()
        );

        var orderResponse = mockMvc.perform(
            post("/api/orders/place")
                .header("Authorization", "Bearer " + validToken)

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Order successfully created"))
            .andReturn();

        System.out.println("   ✓ Order placed");

        // Step 7: View order history
        System.out.println("Step 7: Viewing order history...");
        mockMvc.perform(
            get("/api/orders/my-orders")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
        System.out.println("   ✓ Order history retrieved");

        // Step 8: Add product review //// NEED TO CALL API, AUTH REQUIRED
        System.out.println("Step 8: Adding product review...");
        Review review = new Review();
        review.setRating(5);
        review.setComment("Excellent E2E test product!");

        mockMvc.perform(
            post("/api/reviews/{productId}", testProductId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review))
        )
            .andExpect(status().isOk());
        System.out.println("   ✓ Review added");

        // Step 9: Update profile
        System.out.println("Step 9: Updating user profile...");
        UserUpdateDTO updateDTO = new UserUpdateDTO(
            "Updated E2E User",
            "updatede2e",
            null,
            null,
            testAddress,
            null
        );

        mockMvc.perform(
            put("/api/users/profile")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Profile updated successfully"));
        System.out.println("   ✓ Profile updated");

        // Step 10: Logout
        System.out.println("Step 10: Logging out...");
        mockMvc.perform(
            post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));
        System.out.println("   ✓ User logged out");

        System.out.println("\n========================================");
        System.out.println("✅ E2E TEST COMPLETED SUCCESSFULLY!");
        System.out.println("========================================\n");
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n🧹 Cleaning up test data...");
        // Clean up test data
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }
}