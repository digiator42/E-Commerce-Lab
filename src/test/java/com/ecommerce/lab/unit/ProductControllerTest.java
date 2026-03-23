package com.ecommerce.lab.unit;

import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.dto.ProductRequestDTO;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}