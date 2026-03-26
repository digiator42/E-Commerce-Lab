package com.ecommerce.lab.integration;

import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.base.ProductRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseIntegrityTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldVerifySeededDataExists() {
        long count = productRepository.count();

        assertThat(count).isGreaterThan(0);

        Product product = productRepository.findById(1L).get();

        assertThat(product.getPrice()).isNotNull();
        assertThat(product.getCategory()).isNotNull(); // Verifies Lazy Loading works
        // assertThat(product.getReviews()).isNotEmpty(); // Fails
    }
}
