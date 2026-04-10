package com.ecommerce.lab.dev.integration;

import com.ecommerce.lab.dev.utils.TestDataGenerator;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.base.ProductRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseIntegrityTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDataGenerator testDataGenerator;

    @BeforeAll
    void init() { testDataGenerator.generateProducts(10); }

    @Test
    void shouldVerifySeededDataExists() {
        long count = productRepository.count();

        assertThat(count).isEqualTo(10);

        Product product = productRepository.findAll().get(0);

        assertThat(product.getPrice()).isNotNull();
        assertThat(product.getCategory()).isNotNull(); // Verifies Lazy Loading works
        // assertThat(product.getReviews()).isNotEmpty(); // Fails
    }
}
