package com.ecommerce.lab.unit;

import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.controller.ProductController;
import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.exception.ProductNotFoundException;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.service.ProductService;
import com.ecommerce.lab.utils.TestDataFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJson
class ProductControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    @WithMockUser
    @DisplayName("Should handle non-existent product")
    void shouldReturn404_WhenProductNotFound() throws Exception {
        Long productId = 999L;
        when(productService.getProduct(eq(productId), any()))
            .thenThrow(new ProductNotFoundException("Product Not Found"));

        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isNotFound());

        verify(productService, times(1)).getProduct(eq(productId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create product successfully")
    void shouldCreateProduct() throws Exception {
        ProductRequestDTO request = TestDataFactory.createValidProductRequest();
        Product product = TestDataFactory.createTestProduct();

        when(productService.createProduct(any(ProductRequestDTO.class)))
            .thenReturn(product);

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject product with invalid data")
    void shouldRejectInvalidProduct() throws Exception {
        // Given
        ProductRequestDTO invalidRequest = new ProductRequestDTO(
            "", // NotBlank
            "Description",
            5,
            10.0,
            ""
        );

        // When/Then
        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return paginated products")
    void shouldReturnPaginatedProducts() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        ProductResponseDTO productResponse = TestDataFactory.createTestProductResponse(true);
        Page<ProductResponseDTO> productPage = new PageImpl<>(List.of(productResponse));

        when(
            productService.getProductsPage(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("newest"),
                any(pageable.getClass()),
                any()
            )
        )
            .thenReturn(productPage);

        // When/Then
        mockMvc.perform(
            get("/api/products/custom")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value("Test Product"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andDo(print());

        verify(productService, times(1)).getProductsPage(
            isNull(), isNull(), isNull(), isNull(), isNull(),
            eq("newest"),
            any(pageable.getClass()),
            any()
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Should filter products by search term")
    void shouldFilterProductsBySearch() throws Exception {
        // Given
        String searchTerm = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        ProductResponseDTO productResponse = TestDataFactory.createTestProductResponse(true);
        Page<ProductResponseDTO> productPage = new PageImpl<>(List.of(productResponse));

        when(
            productService.getProductsPage(
                eq(searchTerm), isNull(), isNull(), isNull(), isNull(),
                eq("newest"),
                any(pageable.getClass()),
                any()
            )
        )
            .thenReturn(productPage);

        mockMvc.perform(
            get("/api/products/custom")
                .param("search", searchTerm)
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should filter products with multiple criteria")
    void shouldFilterWithMultipleCriteria() throws Exception {
        // Given
        Page<ProductResponseDTO> productPage = new PageImpl<>(
            List.of(TestDataFactory.createTestProductResponse(true))
        );

        when(
            productService.getProductsPage(
                anyString(), anyList(), anyDouble(), anyDouble(),
                anyDouble(), anyString(), any(), any()
            )
        ).thenReturn(productPage);

        // When/Then
        mockMvc.perform(
            get("/api/products/custom")
                .param("search", "Test")
                .param("category", "Electronics")
                .param("minPrice", "10")
                .param("maxPrice", "100")
                .param("minRating", "3")
                .param("sort", "price_asc")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Test Product"))
            .andDo(print());
    }
}